# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Deep-Digest-RSS is a news aggregation platform with AI-powered summarization. It pulls RSS feeds and uses AI engines (ChatGPT, DeepSeek, Anthropic, Ollama) to generate interest scores and summaries. Monorepo with four services.

## Build & Development Commands

### Backend (news-backend) — Spring Boot 4 + Java 21
```bash
cd news-backend
./mvnw spring-boot:run              # Run on port 8080
./mvnw test                         # Run all tests
./mvnw test -Dtest=ClassName        # Run single test class
./mvnw test -Dtest=ClassName#method # Run single test method
./mvnw spotless:check               # Check Java formatting
./mvnw spotless:apply               # Fix Java formatting (Google Java Format)
```

### Frontend (news-frontend) — Vue 3 + TypeScript + Vite
```bash
cd news-frontend
npm run dev          # Dev server on port 5173
npm run build        # Production build (type-check + vite build)
npm run type-check   # TypeScript checking only
npm run lint         # ESLint with auto-fix
npm run format       # Prettier formatting
```

### Scraper (scraper) — Python 3
```bash
cd scraper
python main.py              # Default: continuous RSS fetching + AI processing
python main.py taggroups    # Generate tag groups
```

### Auth (auth) — Express.js + Redis
```bash
cd auth
node src/index.js
```

### Full Stack via Docker
```bash
cp .env.example .env        # Configure API keys
docker compose up --build
```

For local DB without Docker Compose:
```bash
docker run -d --name mariadb -e MARIADB_ROOT_PASSWORD=root -e MARIADB_DATABASE=news_prod -e MARIADB_USER=news -e MARIADB_PASSWORD=news -p 3306:3306 mariadb:latest
```

## Architecture

### Service Responsibilities

- **news-backend**: REST API (`/api/v1/`), MariaDB via Flyway migrations, Spring Security (basic auth + cookie + query param auth), scheduled daily digest (19:00 Berlin time → Discord), Prometheus metrics, Swagger at `/swagger-ui/`
- **news-frontend**: SPA consuming the backend API. Pinia store with localStorage persistence. Light/dark theming, font customization, time-based news sections (Morning/Afternoon/Night), single-article scroll-snap view
- **scraper**: Polls backend for unprocessed feed items, downloads page content, extracts text, generates AI summary + interest score, pushes results back. Pluggable AI engines configured via env vars
- **auth**: Session management with Redis. Validates basic auth credentials, issues session tokens stored in cookies (90-day default lifetime)

### Data Flow

1. Backend fetches RSS feeds on schedule → creates `feed_item_to_process` queue entries
2. Scraper polls `/api/v1/feed-item-to-process/next` → downloads + AI-processes → POSTs result as news item
3. Frontend fetches `/api/v1/news?date=...` → displays by time section with voting

### Database

MariaDB with Flyway migrations in `news-backend/src/main/resources/db/migration/`. Key tables: `news`, `feed`, `feed_item_to_process`, `user`, `tags`, `tag_group`, `news_vote`.

## Code Style

- **Java**: Google Java Format via Spotless. Copyright header required: `/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */`. Lombok for boilerplate, ModelMapper for DTO conversion.
- **Frontend**: Vue Composition API with `<script setup lang="ts">`, scoped CSS, 2-space indent, no semicolons, single quotes (Prettier).
- **Commits**: Conventional commits (e.g., `chore(deps):`, `fix(deps):`, `feat(news):`)

## Pre-commit Hooks

Runs automatically: gitleaks + truffleHog (secret scanning), trailing whitespace/merge conflict checks, `spotless:check` on Java files, `eslint + type-check` on frontend files.

## Deployment

- **Helm charts** in `helm/deep-digest-rss/` deploy to Kubernetes (registry.oglimmer.com, domain: news.oglimmer.com)
- **Renovate** manages dependency updates with auto-merge for minor/patch
