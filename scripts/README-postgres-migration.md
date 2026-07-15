# MariaDB → PostgreSQL cutover

One-time runbook for moving `news_prod` off the in-cluster MariaDB StatefulSet and onto the
shared `postgres` service in the `default` namespace (the same instance the `wiki` app uses).

The app side of the move is already merged: the backend speaks `jdbc:postgresql://`, the Flyway
history is a single PostgreSQL baseline (`V1.0.0__init.sql`), and the Helm chart points
`DB_HOST` at `postgres.default.svc.cluster.local`. What remains is provisioning the database
and copying ~301k news / ~362k queue rows across.

## Order matters

Flyway must create the schema — do not load `V1.0.0__init.sql` with `psql` by hand. Flyway
records its history table during the migration; if the tables already exist without that
history, the backend refuses to start with *"Found non-empty schema(s) 'public' but no schema
history table"*.

## 1. Provision database and role

Run against the central postgres (superuser password is in the `postgres` secret / the
`helm-install-postgres` HelmChart values in `kube-system`):

```sql
CREATE ROLE "news-app" LOGIN PASSWORD '<new-strong-password>';
CREATE DATABASE news_prod OWNER "news-app";
```

The username `news-app` matches what `helm/SEALED-SECRETS.md` already documents and mirrors the
`wiki-app` convention.

## 2. Re-seal the DB credentials

`news-secrets` still holds the MariaDB credentials. Re-seal `SPRING_DATASOURCE_USERNAME` =
`news-app` and `SPRING_DATASOURCE_PASSWORD` = the password from step 1, following
`helm/SEALED-SECRETS.md`. These env vars override the `application.yml` defaults.

## 3. Stop writers

Scale the writers down so the source stops changing mid-copy — otherwise rows scraped during
the copy are lost:

```bash
kubectl scale deploy/news-backend deploy/news-scraper deploy/news-taggroupper --replicas=0
```

## 4. Create the schema

Bring the backend up once against the (empty) PostgreSQL database. Flyway applies `V1.0.0` and
writes `flyway_schema_history`. Confirm, then scale back to 0:

```bash
kubectl scale deploy/news-backend --replicas=1
kubectl logs deploy/news-backend | grep -i flyway   # expect: Successfully applied 1 migration
kubectl scale deploy/news-backend --replicas=0
```

## 5. Copy the data

`migrate_mariadb_to_postgres.py` truncates each target table and bulk-loads the MariaDB rows,
then fast-forwards every identity sequence past the copied ids. Run it somewhere that can reach
both databases (a throwaway pod in the cluster is simplest):

```bash
pip install pymysql psycopg2-binary
MYSQL_HOST=mariadb MYSQL_USER=root MYSQL_PASSWORD=<mariadb-root-pw> \
PG_HOST=postgres.default.svc.cluster.local PG_USER=news-app PG_PASSWORD=<new-password> \
    python3 migrate_mariadb_to_postgres.py --dry-run   # counts only, writes nothing
```

Drop `--dry-run` to perform the copy. It runs in one transaction: on any error the target is
rolled back and left untouched, and re-running is safe. It finishes by printing a per-table
source-vs-target row count — every line must say `ok`.

The truncate also clears the reference rows `V1.0.0` seeds (roles, service accounts, the
scraper API key, the two default feeds). That is deliberate: the MariaDB copies of those rows
carry the real hashes and the original primary keys that everything else references.

## 6. Start up and verify

```bash
kubectl scale deploy/news-backend deploy/news-scraper deploy/news-taggroupper --replicas=1
```

Check that today's news renders on https://news.oglimmer.com, that login works, and that the
scraper picks up queue items. `StartupPasswordSyncer` rewrites the service-account passwords and
the scraper API key hash from the env vars on boot, as it did on MariaDB.

MCP clients (the Claude.ai connector) keep working: `oauth2_registered_client` /
`oauth2_authorization` are copied, so existing tokens and registrations survive.

## 7. Afterwards

Leave the MariaDB StatefulSet running but idle until you are satisfied, then remove it and its
`longhorn` PVC. It is the only rollback: re-point `config.dbHost` back at `mariadb` and revert
the backend image.

## Notes on the tricky bits

**Timezones.** MariaDB stored `DATETIME(6)`, which has no zone; the server ran UTC and the
backend JVM had no `TZ` override, so the values are UTC wall-clock. The PostgreSQL columns are
`TIMESTAMPTZ` and the script stamps every naive datetime as UTC. Interpreting them as
Europe/Berlin instead would shift every article by 1–2 hours and scramble the
morning/afternoon/night sections.

**NUL bytes.** PostgreSQL `text` cannot store `\x00`, MariaDB `TEXT` can, and scraped article
bodies occasionally contain them. The script strips them; without that the whole batch aborts.

**`@Lob`.** On PostgreSQL, Hibernate maps `@Lob String` to `oid` (large-object) rather than
`text`. The entities use `@JdbcTypeCode(SqlTypes.LONGVARCHAR)` instead — do not reintroduce
`@Lob` on a `String` field.

**`process_state`.** Was a MariaDB `enum`; it is now `VARCHAR` plus a CHECK constraint, which
is what Hibernate's `@Enumerated(STRING)` validates against.
