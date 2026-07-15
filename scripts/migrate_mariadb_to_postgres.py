#!/usr/bin/env python3
"""One-shot data migration from the legacy MariaDB news_prod to PostgreSQL.

The PostgreSQL schema must already exist: run the backend (or Flyway) once against the target
so V1.0.0__init.sql is applied, then run this. Each table is TRUNCATEd before it is loaded,
which also clears the reference rows V1.0.0 seeds (roles, service accounts, the scraper API
key, the two default feeds); the MariaDB copies of those rows carry the real password and key
hashes and land with their original primary keys, so the seeds must not survive alongside them.

Timezone handling is the subtle part. MariaDB stored these columns as DATETIME(6), which has
no zone. The server ran in UTC and the backend JVM had no TZ override, so the MariaDB JDBC
driver wrote UTC wall-clock values. The PostgreSQL columns are TIMESTAMPTZ, so every naive
datetime read from MariaDB is stamped as UTC here. Assuming anything else (e.g. Europe/Berlin)
would silently shift every article by an hour or two and scramble the morning/afternoon/night
sections in the UI.

Usage:
    pip install pymysql psycopg2-binary
    MYSQL_HOST=... MYSQL_PASSWORD=... PG_HOST=... PG_PASSWORD=... \
        python3 migrate_mariadb_to_postgres.py [--dry-run]

Re-running is safe: each table is truncated on the target before it is loaded.
"""

import argparse
import datetime
import os
import sys
from decimal import Decimal

import psycopg2
import psycopg2.extras
import pymysql
import pymysql.cursors

UTC = datetime.timezone.utc

# FK-safe order: every table appears after the tables it references.
TABLES = [
    "roles",
    "users",
    "user_roles",
    "api_keys",
    "api_key_roles",
    "feed",
    "feed_item_to_process",
    "news",
    "tags",
    "news_tags",
    "tag_group",
    "tag_group_tags",
    "news_vote",
    "daily_digest",
    "oauth2_registered_client",
    "oauth2_authorization_consent",
    "oauth2_authorization",
]

# Tables with an identity column whose sequence must be fast-forwarded past the copied ids.
SEQUENCE_TABLES = [
    "roles",
    "users",
    "api_keys",
    "feed",
    "feed_item_to_process",
    "news",
    "tags",
    "tag_group",
    "news_vote",
    "daily_digest",
]

BATCH_SIZE = 1000


def log(msg):
    print(msg, flush=True)


def pg_column_types(pg, table):
    """Map column -> PostgreSQL type, used to drive value conversion."""
    with pg.cursor() as cur:
        cur.execute(
            """
            SELECT column_name, data_type
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = %s
            ORDER BY ordinal_position
            """,
            (table,),
        )
        return {row[0]: row[1] for row in cur.fetchall()}


def convert(value, pg_type):
    """Adapt one MariaDB value to what the PostgreSQL column expects."""
    if value is None:
        return None

    # DATETIME(6) -> TIMESTAMPTZ. The value is UTC wall-clock (see module docstring).
    if pg_type == "timestamp with time zone" and isinstance(value, datetime.datetime):
        return value.replace(tzinfo=UTC) if value.tzinfo is None else value

    # TINYINT(1) -> BOOLEAN. pymysql surfaces these as ints.
    if pg_type == "boolean" and isinstance(value, int):
        return bool(value)

    if isinstance(value, str):
        # PostgreSQL text cannot hold NUL bytes; MariaDB TEXT can. Scraped article bodies
        # occasionally carry them, and the insert would abort the whole batch.
        if "\x00" in value:
            return value.replace("\x00", "")

    if isinstance(value, Decimal):
        return value

    return value


def copy_table(my, pg, table, dry_run):
    types = pg_column_types(pg, table)
    if not types:
        raise RuntimeError(f"target table {table!r} does not exist -- run Flyway first")

    with my.cursor() as cur:
        cur.execute(f"SELECT COUNT(*) AS c FROM `{table}`")
        source_count = cur.fetchone()["c"]

    if dry_run:
        log(f"  {table:<32} {source_count:>8} rows (dry-run, not copied)")
        return source_count, 0

    # Column order is taken from the source so the SELECT and INSERT always line up.
    with my.cursor() as cur:
        cur.execute(f"SHOW COLUMNS FROM `{table}`")
        columns = [r["Field"] for r in cur.fetchall()]

    unknown = [c for c in columns if c not in types]
    if unknown:
        raise RuntimeError(f"{table}: columns missing on target: {unknown}")

    col_types = [types[c] for c in columns]
    pg_cols = ", ".join(f'"{c}"' for c in columns)
    my_cols = ", ".join(f"`{c}`" for c in columns)
    insert = f'INSERT INTO "{table}" ({pg_cols}) VALUES %s'

    with pg.cursor() as pcur:
        pcur.execute(f'TRUNCATE TABLE "{table}" CASCADE')

    # Server-side cursor: news/feed_item_to_process are ~300k rows with large text bodies,
    # so the result set must not be buffered client-side.
    stream = my.cursor(pymysql.cursors.SSDictCursor)
    stream.execute(f"SELECT {my_cols} FROM `{table}`")

    copied = 0
    batch = []
    with pg.cursor() as pcur:
        while True:
            rows = stream.fetchmany(BATCH_SIZE)
            if not rows:
                break
            for row in rows:
                batch.append(
                    tuple(convert(row[c], t) for c, t in zip(columns, col_types))
                )
            psycopg2.extras.execute_values(pcur, insert, batch, page_size=BATCH_SIZE)
            copied += len(batch)
            batch = []
            if copied % 50000 == 0:
                log(f"  {table:<32} {copied:>8}/{source_count} ...")
    stream.close()

    log(f"  {table:<32} {copied:>8}/{source_count} rows copied")
    return source_count, copied


# (child table, child column, parent table) for every foreign key in the schema. Checked after
# a load that ran with FK triggers disabled.
FOREIGN_KEYS = [
    ("user_roles", "user_id", "users"),
    ("user_roles", "role_id", "roles"),
    ("api_key_roles", "api_key_id", "api_keys"),
    ("api_key_roles", "role_id", "roles"),
    ("feed_item_to_process", "feed_id", "feed"),
    ("news", "feed_id", "feed"),
    ("news", "original_feed_item_id", "feed_item_to_process"),
    ("news_tags", "news_id", "news"),
    ("news_tags", "tags_id", "tags"),
    ("tag_group_tags", "tag_group_id", "tag_group"),
    ("tag_group_tags", "tags_id", "tags"),
    ("news_vote", "news_id", "news"),
    ("news_vote", "user_id", "users"),
]


def verify_foreign_keys(pg):
    """Re-check referential integrity, since the load may have bypassed the FK triggers."""
    orphans = []
    with pg.cursor() as cur:
        for child, column, parent in FOREIGN_KEYS:
            cur.execute(
                f'SELECT COUNT(*) FROM "{child}" c '
                f'LEFT JOIN "{parent}" p ON p.id = c."{column}" '
                f'WHERE c."{column}" IS NOT NULL AND p.id IS NULL'
            )
            count = cur.fetchone()[0]
            if count:
                orphans.append(f"{child}.{column} -> {parent}: {count} orphan(s)")
    if orphans:
        for line in orphans:
            log(f"  ORPHAN {line}")
        raise RuntimeError("referential integrity check failed")
    log(f"  foreign keys verified ({len(FOREIGN_KEYS)} constraints, no orphans)")


def reset_sequences(pg):
    with pg.cursor() as cur:
        for table in SEQUENCE_TABLES:
            # setval to MAX(id); is_called=false when empty so the sequence still starts at 1.
            cur.execute(
                f"""
                SELECT setval(
                    pg_get_serial_sequence('{table}', 'id'),
                    COALESCE((SELECT MAX(id) FROM "{table}"), 1),
                    (SELECT MAX(id) IS NOT NULL FROM "{table}")
                )
                """
            )
    log("  sequences reset to MAX(id)")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="count source rows and validate the target schema without writing",
    )
    args = parser.parse_args()

    my = pymysql.connect(
        host=os.environ.get("MYSQL_HOST", "mariadb"),
        port=int(os.environ.get("MYSQL_PORT", "3306")),
        user=os.environ.get("MYSQL_USER", "root"),
        password=os.environ["MYSQL_PASSWORD"],
        database=os.environ.get("MYSQL_DB", "news_prod"),
        cursorclass=pymysql.cursors.DictCursor,
        charset="utf8mb4",
    )
    pg = psycopg2.connect(
        host=os.environ.get("PG_HOST", "postgres"),
        port=int(os.environ.get("PG_PORT", "5432")),
        user=os.environ.get("PG_USER", "news-app"),
        password=os.environ["PG_PASSWORD"],
        dbname=os.environ.get("PG_DB", "news_prod"),
    )
    pg.autocommit = False

    # Pin both sides to UTC so no driver applies a local-time conversion of its own.
    with my.cursor() as cur:
        cur.execute("SET time_zone = '+00:00'")
        # This script reads from MariaDB with an unbuffered cursor and stops reading while it
        # writes each batch into PostgreSQL. On the wide/foreign-keyed tables a batch can take
        # minutes, and MariaDB drops the connection once it has been unable to write to us for
        # net_write_timeout (default 60s) -- surfacing as "(2013, Lost connection to MySQL
        # server during query)" partway through the copy. Give the server room to wait.
        cur.execute("SET SESSION net_write_timeout = 3600")
        cur.execute("SET SESSION net_read_timeout = 3600")
        cur.execute("SET SESSION wait_timeout = 28800")
    # Per-row FK trigger checks dominate the run on the join tables (news_tags and
    # tag_group_tags are ~4.9M rows between them), slowing a batch enough that MariaDB times
    # the read side out. session_replication_role=replica skips them for this session; it needs
    # superuser, so fall back to a normal (slower) load when connected as the app role. The
    # source enforced the same foreign keys, the copy runs in dependency order, and
    # verify_foreign_keys() re-checks for orphans afterwards.
    pg.autocommit = True
    with pg.cursor() as cur:
        cur.execute("SET TIME ZONE 'UTC'")
        try:
            cur.execute("SET session_replication_role = replica")
            log("  FK triggers disabled for this session (superuser)")
        except psycopg2.Error as exc:
            log(f"  note: keeping FK checks on ({str(exc).strip()}); copy will be slower")
    pg.autocommit = False

    log(f"source: {my.host}/{os.environ.get('MYSQL_DB', 'news_prod')} (MariaDB)")
    log(f"target: {pg.get_dsn_parameters()['host']}/{pg.get_dsn_parameters()['dbname']} (PostgreSQL)")
    log("")

    totals = []
    try:
        for table in TABLES:
            totals.append((table,) + copy_table(my, pg, table, args.dry_run))
        if args.dry_run:
            pg.rollback()
            log("\ndry-run: rolled back, nothing written")
            return 0
        verify_foreign_keys(pg)
        reset_sequences(pg)
        pg.commit()
    except Exception:
        pg.rollback()
        log("\nFAILED -- transaction rolled back, target left unchanged")
        raise
    finally:
        my.close()
        pg.close()

    log("\nrow-count verification")
    bad = [t for t in totals if t[1] != t[2]]
    for table, src, dst in totals:
        mark = "ok" if src == dst else "MISMATCH"
        log(f"  {table:<32} source={src:<8} target={dst:<8} {mark}")
    if bad:
        log("\nrow counts do not match -- investigate before switching traffic")
        return 1
    log("\nall tables match")
    return 0


if __name__ == "__main__":
    sys.exit(main())
