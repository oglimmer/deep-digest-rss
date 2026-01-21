# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
npm run dev          # Start Vite development server
npm run build        # Production build (type-check + vite build)
npm run type-check   # Vue TypeScript type checking only
npm run lint         # ESLint with auto-fix
npm run format       # Prettier formatting on src/
```

## Architecture

This is a Vue 3 + TypeScript + Vite frontend for DeepDigestRSS - a news aggregation platform with AI-powered summarization.

### State Management

Pinia store (`src/stores/data.ts`) manages all application state with localStorage persistence:
- Authentication (email, token, login status)
- News entries and feed data
- Filter selections (feeds, tag groups)
- Date navigation
- Theme settings (dark mode, font family 12-24px, view mode)

### Component Structure

- **App.vue** - Root layout with header (title + ThemeToggle) and NewsList
- **NewsList.vue** - Main controller: date navigation, filters, renders sections
- **NewsSection.vue** - Renders Morning/Afternoon/Night sections
- **NewsItem.vue** - Individual article display with voting
- **SingleNewsView.vue** - Full-screen article paging mode (scroll-snap)
- **ThemeToggle.vue** - Settings bar: font selector, size controls, dark mode, view toggle
- **LoginForm.vue** - Authentication modal

### Styling

CSS Variables in `src/assets/main.css` control theming:
- Light/dark themes via `[data-theme="dark"]`
- Font families via `[data-font="..."]` (system, georgia, palatino, charter, verdana)
- Font size via `--font-size-base` (12-24px)
- Mobile breakpoints at 480px and 600px

### View Modes

**Desktop vs Mobile (breakpoint: 480px)**
- Desktop: Shows full font size controls (A-, size, A+) in ThemeToggle
- Mobile: Hides font size controls, shows single "A" button that cycles through sizes (increments by 2, wraps from 24 back to 12)
- Mobile: Reduced body padding (16px → 8px in main.css)
- Mobile: Compact header elements with smaller font sizes

**Normal View vs Single Article View**
- Controlled by `store.singleNewsMode` toggle
- Normal view: NewsList renders NewsSection components (Morning/Afternoon/Night) with scrollable article list
- Single article view: SingleNewsView renders as fixed full-screen overlay (z-index: 50) with CSS scroll-snap for one-article-per-page navigation
- Single article view respects `--font-size-base` setting (title: base × 1.3 desktop, base × 1.2 mobile)
- Single article view has its own exit button (✕) in top-right corner since it covers the normal header

### API Integration

REST client in `src/services/remote.ts`:
- Base URL: `__API_URL__` (default: http://localhost:8080)
- Basic auth: `__API_USER__` / `__API_PASSWORD__` (default: "read")
- Endpoints: `/api/v1/feed`, `/api/v1/news`, `/api/v1/tag-group`, `/api/v1/auth/login`, `/api/v1/news/{id}/vote`

## Code Style

- Vue Composition API with `<script setup lang="ts">`
- Scoped CSS in components
- 2-space indentation, no semicolons, single quotes (Prettier)
- Conventional commits (e.g., `chore(news):`, `fix(theme):`)

## Pre-commit Hooks

The root `.pre-commit-config.yaml` runs `npm run lint && npm run type-check` on frontend changes.
