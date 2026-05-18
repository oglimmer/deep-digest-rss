# deep-digest-rss

AI-powered RSS news aggregator. Pulls articles from RSS feeds, downloads the
full page content, and uses an AI engine (e.g. ChatGPT) to generate per-article
summaries and an interest score. A daily digest is posted to Discord at 19:00
Europe/Berlin.

Live instance: https://news.oglimmer.com

## Architecture

Monorepo with three services:

| Service          | Stack                              | Port  | Role                                                                 |
|------------------|------------------------------------|-------|----------------------------------------------------------------------|
| `news-backend`   | Spring Boot 4, Java 21, MariaDB    | 8080  | REST API (`/api/v1/`), Flyway migrations, scheduled jobs, auth       |
| `news-frontend`  | Vue 3, TypeScript, Vite, Pinia     | 5173  | SPA with light/dark themes, time-of-day sections, voting             |
| `scraper`        | Python 3                           | -     | Polls backend queue, fetches pages, runs AI summarization            |

Shared infra: MariaDB (data), Redis (HTTP sessions).

### Data flow

1. Backend fetches RSS feeds on a schedule and enqueues `feed_item_to_process` rows.
2. Scraper polls `/api/v1/feed-item-to-process/next`, downloads the page, runs
   the configured AI engine, and POSTs the resulting news item back.
3. Frontend calls `/api/v1/news?date=...` and renders by time section.

### Authentication

- **Browser users**: Spring Session with a Redis-backed `DDRSS_SESSION` cookie.
- **Service-to-service** (scraper → backend): `X-API-Key` header, value from
  `SCRAPER_API_KEY`.

## Quick start (Docker Compose)

```bash
cp .env.example .env          # then fill in API_KEY with your OpenAI key
docker compose up --build
```

Required variables in `.env`:

| Variable            | Example                  | Notes                                  |
|---------------------|--------------------------|----------------------------------------|
| `URL`               | `http://backend:8080/`   | Backend URL the scraper talks to       |
| `MODEL`             | `gpt-4o-mini`            | AI model name                          |
| `API_KEY`           | `sk-...`                 | OpenAI API key                         |
| `GENERATION_ENGINE` | `chatgpt`                | Currently the only supported engine    |

Then open:

- Frontend: http://localhost:5173/
- Backend API: http://localhost:8888/
- Swagger UI: http://localhost:8888/swagger-ui/

## Local development

### Database & Redis only (when running services on the host)

```bash
docker run -d --name mariadb \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=news_prod \
  -e MARIADB_USER=news \
  -e MARIADB_PASSWORD=news \
  -p 3306:3306 mariadb:latest

docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### Backend

```bash
cd news-backend
./mvnw spring-boot:run            # http://localhost:8080
./mvnw test                       # all tests
./mvnw spotless:apply             # Google Java Format
```

### Frontend

```bash
cd news-frontend
npm install
npm run dev                       # http://localhost:5173
npm run build                     # type-check + production build
npm run lint                      # ESLint --fix
```

### Scraper

```bash
cd scraper
pip install -r requirements.txt
python main.py                    # continuous: fetch RSS + AI process
python main.py taggroups          # one-shot: regenerate tag groups
```

The scraper reads config from `scraper/.env` (see `scraper/.env.example`) unless
`NO_ENV_FILE=true` is set.

## Tests, formatting, and hooks

- Java: `./mvnw test`, `./mvnw spotless:check` / `spotless:apply`.
- Frontend: `npm run type-check`, `npm run lint`, `npm run format`.
- Pre-commit hooks run gitleaks + trufflehog (secret scanning), trailing
  whitespace / merge-conflict checks, `spotless:check` on staged Java files,
  and `eslint` + `type-check` on staged frontend files.
- A GitHub Actions workflow runs the same checks on PRs.

## Deployment

- Helm charts in `helm/deep-digest-rss/` target Kubernetes
  (registry `registry.oglimmer.com`, domain `news.oglimmer.com`).
- Sealed Secrets workflow described in `helm/SEALED-SECRETS.md`.
- Renovate manages dependency updates (auto-merge for minor/patch).

## Observability

- Prometheus metrics: `/actuator/prometheus`
- Health probes (unauthenticated): `/actuator/health/liveness`,
  `/actuator/health/readiness`
- API docs: `/swagger-ui/`
