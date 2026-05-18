<script setup lang="ts">
import { ref } from 'vue'

type Tab =
  | 'architecture'
  | 'pipeline'
  | 'ai'
  | 'tags'
  | 'digest'
  | 'database'
  | 'deployment'
  | 'overview'
  | 'endpoints'
  | 'dtos'

type Section = 'concepts' | 'reference'
const activeSection = ref<Section>('concepts')
const activeTab = ref<Tab>('architecture')

const conceptTabs: { id: Tab; label: string; kicker: string }[] = [
  { id: 'architecture', label: 'Architecture', kicker: '01' },
  { id: 'pipeline', label: 'Ingestion', kicker: '02' },
  { id: 'ai', label: 'AI Engines', kicker: '03' },
  { id: 'tags', label: 'Tag Groups', kicker: '04' },
  { id: 'digest', label: 'Daily Digest', kicker: '05' },
  { id: 'database', label: 'Database', kicker: '06' },
  { id: 'deployment', label: 'Deployment', kicker: '07' },
]

const referenceTabs: { id: Tab; label: string }[] = [
  { id: 'overview', label: 'Overview' },
  { id: 'endpoints', label: 'Endpoints' },
  { id: 'dtos', label: 'DTOs' },
]

const selectSection = (s: Section) => {
  activeSection.value = s
  activeTab.value = s === 'concepts' ? 'architecture' : 'overview'
}

const services = [
  {
    name: 'news-backend',
    stack: 'Spring Boot 4 · Java 21',
    port: '8080',
    role: 'REST API · scheduling · auth gateway',
    accent: 'svc-back',
    glyph: 'B',
    image: 'news-backend',
  },
  {
    name: 'news-frontend',
    stack: 'Vue 3 · Nginx',
    port: '80',
    role: 'SPA · reader UI · this page',
    accent: 'svc-front',
    glyph: 'F',
    image: 'news-frontend',
  },
  {
    name: 'news-auth',
    stack: 'Express · Redis',
    port: '3000',
    role: 'Session tokens · cookie issuer',
    accent: 'svc-auth',
    glyph: 'A',
    image: 'news-auth',
  },
  {
    name: 'news-scraper',
    stack: 'Python 3.11 · OpenAI SDK',
    port: '—',
    role: 'Pulls queue · AI summary + score',
    accent: 'svc-scrape',
    glyph: 'S',
    image: 'news-scraper',
    cmd: './run.sh',
  },
  {
    name: 'news-taggroupper',
    stack: 'Python 3.11 · OpenAI SDK',
    port: '—',
    role: 'Regroups raw tags into 8 categories',
    accent: 'svc-tag',
    glyph: 'T',
    image: 'news-scraper',
    cmd: './run.sh taggroups',
    note: 'Same image as news-scraper, different command',
  },
]

const pipelineStates = [
  { id: 'NEW', desc: 'Just enqueued by the feed fetcher', tone: 'state-new' },
  { id: 'IN_PROGRESS', desc: 'Scraper has dequeued and is downloading', tone: 'state-prog' },
  { id: 'DONE', desc: 'AI summary stored, news row created', tone: 'state-done' },
  { id: 'ERROR', desc: 'Retryable: re-queued (up to 5 attempts)', tone: 'state-err' },
  { id: 'FAILED', desc: 'Terminal: exceeded retry budget', tone: 'state-fail' },
]

const aiEngines = [
  {
    name: 'Summary',
    file: 'scraper/generate_ai_summary.py',
    desc: 'Reads article text, returns a JSON object enforced by schema.',
    schema: `{
  "summary": "string",
  "tags": ["string"],         // max 3
  "advertising": boolean,
  "timely": boolean,
  "impact_scope": "global | international |
                  europa | deutschland |
                  regional | branche"
}`,
  },
  {
    name: 'Relevance',
    file: 'scraper/generate_ai_interest.py',
    desc: 'Scores 0–1 against the user’s top 7 voted articles. Above 0.8 gets the "Interessant" tag.',
    schema: `{ "interest_score": 0.0 .. 1.0 }`,
  },
]

const aiEnvVars = [
  { key: 'GENERATION_ENGINE', val: 'chatgpt', note: 'Selects the active engine' },
  { key: 'MODEL', val: 'gpt-4o-mini', note: 'OpenAI model id' },
  { key: 'API_KEY', val: 'sk-…', note: 'OpenAI key' },
  { key: 'ANTHROPIC_MODEL', val: 'claude-haiku-4-5-20251001', note: 'Anthropic fallback' },
  { key: 'ANTHROPIC_API_KEY', val: 'sk-ant-…', note: 'Anthropic key' },
]

const tagCategories = [
  'Technology',
  'Environment',
  'Politics',
  'Economy',
  'Science',
  'Culture',
  'Health',
  'Sports',
]

const migrations = [
  { ver: 'V1.0.0', what: 'news table — id, title, text, url, ref_id, created_on' },
  { ver: 'V2.0.0', what: 'feed_item table' },
  { ver: 'V3.0.0', what: 'feed cookie auth (gated RSS sources)' },
  { ver: 'V4.0.0', what: 'tag_group · tags · news_tags · tag_group_tags' },
  { ver: 'V6.0.0', what: 'users — email, timezone, votes' },
  { ver: 'V8.0.0', what: 'news_vote (user ↔ article)' },
  { ver: 'V13.0.0', what: 'news.timely + news.impact_scope' },
  { ver: 'V14.0.0', what: 'daily_digest — content LONGTEXT, created_on' },
]

const deployTargets = [
  {
    name: 'Local Dev',
    cmd: 'docker compose up --build',
    desc: 'compose.yml wires all four services plus MariaDB on 3306.',
    note: 'Fastest path to a running stack',
  },
  {
    name: 'Kubernetes',
    cmd: 'helm install dd helm/deep-digest-rss',
    desc: 'Chart pins to nodeSelector dedicated=news-system, TLS via cert-manager, images from registry.oglimmer.com.',
    note: 'Production target: news.oglimmer.com',
  },
  {
    name: 'Standalone',
    cmd: './mvnw spring-boot:run',
    desc: 'Backend on 8080, frontend on 5173 (npm run dev). Useful when iterating on one service.',
    note: 'Requires MariaDB running separately',
  },
]

const copiedId = ref<string | null>(null)
const copy = async (text: string, id: string) => {
  await navigator.clipboard.writeText(text.trim())
  copiedId.value = id
  setTimeout(() => {
    if (copiedId.value === id) copiedId.value = null
  }, 1800)
}

const expanded = ref<string | null>(null)
const toggle = (id: string) => {
  expanded.value = expanded.value === id ? null : id
}

const BASE_URL = 'https://news.oglimmer.com'

const btoa = (s: string) => window.btoa(s)

const endpoints = [
  {
    group: 'Auth',
    items: [
      {
        id: 'auth-login',
        method: 'POST',
        path: '/api/v1/auth/login',
        role: 'public',
        summary: 'Authenticate and obtain an auth token',
        params: [],
        request: `{
  "email": "user@example.com",
  "password": "secret"
}`,
        response: `{
  "authToken": "abc123def456..."
}`,
        notes: 'The returned authToken is used as the password in subsequent Basic Auth calls: Authorization: Basic base64(email:authToken)',
      },
      {
        id: 'auth-register',
        method: 'POST',
        path: '/api/v1/auth/register',
        role: 'public',
        summary: 'Register a new user account',
        params: [],
        request: `{
  "email": "user@example.com",
  "password": "secret"
}`,
        response: '200 OK (empty body)',
        notes: '',
      },
    ],
  },
  {
    group: 'News',
    items: [
      {
        id: 'news-list',
        method: 'GET',
        path: '/api/v1/news',
        role: 'READONLY',
        summary: 'List news articles for a given date',
        params: [
          { name: 'date', type: 'string', required: false, desc: 'ISO date (YYYY-MM-DD). Defaults to today.' },
          { name: 'feedIdList', type: 'string', required: false, desc: 'Comma-separated feed IDs to filter by.' },
        ],
        request: '',
        response: `[
  {
    "id": 42,
    "feedId": 1,
    "createdOn": "2025-05-18T08:30:00Z",
    "url": "https://...",
    "text": "AI-generated summary...",
    "title": "Article title",
    "advertising": false,
    "tags": ["tech", "ai"],
    "timely": true,
    "impactScope": "global",
    "voted": false
  }
]`,
        notes: '',
      },
      {
        id: 'news-create',
        method: 'POST',
        path: '/api/v1/news',
        role: 'ADMIN',
        summary: 'Create a new news article (used by scraper)',
        params: [],
        request: `{
  "feedId": 1,
  "originalFeedItemId": 100,
  "text": "Summary text...",
  "advertising": false,
  "tags": ["tech", "ai"],
  "timely": true,
  "impactScope": "global"
}`,
        response: 'NewsDto (see DTOs tab)',
        notes: 'impactScope must be one of: global, international, europa, deutschland, regional, branche',
      },
      {
        id: 'news-vote',
        method: 'POST',
        path: '/api/v1/news/{id}/vote',
        role: 'USER',
        summary: 'Cast a vote on an article',
        params: [
          { name: 'id', type: 'path', required: true, desc: 'News article ID' },
        ],
        request: `{
  "vote": true
}`,
        response: '200 OK (empty body)',
        notes: '',
      },
      {
        id: 'news-by-ref',
        method: 'GET',
        path: '/api/v1/news/by-ref/{id}',
        role: 'READONLY',
        summary: 'Look up a news article by its feed item reference ID',
        params: [
          { name: 'id', type: 'path', required: true, desc: 'Feed item reference ID (string)' },
        ],
        request: '',
        response: 'NewsDto (see DTOs tab)',
        notes: '',
      },
      {
        id: 'news-patch',
        method: 'PATCH',
        path: '/api/v1/news/{id}',
        role: 'ADMIN',
        summary: 'Add tags to an existing news article',
        params: [
          { name: 'id', type: 'path', required: true, desc: 'News article ID' },
        ],
        request: `{
  "tagsToAdd": ["breaking", "politics"]
}`,
        response: 'NewsDto (see DTOs tab)',
        notes: '',
      },
    ],
  },
  {
    group: 'Feed',
    items: [
      {
        id: 'feed-list',
        method: 'GET',
        path: '/api/v1/feed',
        role: 'READONLY',
        summary: 'List all RSS feeds',
        params: [],
        request: '',
        response: `[
  {
    "id": 1,
    "url": "https://example.com/rss",
    "title": "Example Feed",
    "cookie": null,
    "createdOn": "2025-01-01T00:00:00Z"
  }
]`,
        notes: '',
      },
      {
        id: 'feed-patch',
        method: 'PATCH',
        path: '/api/v1/feed/{id}',
        role: 'ADMIN',
        summary: 'Update a feed (e.g. set a cookie for authenticated RSS)',
        params: [
          { name: 'id', type: 'path', required: true, desc: 'Feed ID' },
        ],
        request: `{
  "cookie": "session=abc123"
}`,
        response: 'FeedDto (see DTOs tab)',
        notes: '',
      },
    ],
  },
  {
    group: 'Tag Group',
    items: [
      {
        id: 'taggroup-list',
        method: 'GET',
        path: '/api/v1/tag-group',
        role: 'READONLY',
        summary: 'Get tag groups for a date (used for filtering)',
        params: [
          { name: 'date', type: 'string', required: false, desc: 'ISO date (YYYY-MM-DD). Defaults to today.' },
          { name: 'timeZone', type: 'string', required: false, desc: 'Time zone ID (default: Europe/Berlin).' },
        ],
        request: '',
        response: `{
  "Politics": ["politik", "government", "election"],
  "Technology": ["tech", "ai", "software"]
}`,
        notes: 'Keys are group names; values are arrays of tag strings belonging to that group.',
      },
      {
        id: 'taggroup-raw',
        method: 'GET',
        path: '/api/v1/tag-group/raw',
        role: 'READONLY',
        summary: "List all raw tag strings from today's articles",
        params: [],
        request: '',
        response: '["tech", "ai", "politik", ...]',
        notes: '',
      },
      {
        id: 'taggroup-patch',
        method: 'PATCH',
        path: '/api/v1/tag-group',
        role: 'ADMIN',
        summary: 'Update the tag group mapping',
        params: [],
        request: `{
  "tags": {
    "Politics": ["politik", "government"],
    "Technology": ["tech", "ai"]
  }
}`,
        response: '200 OK or 304 Not Modified',
        notes: '',
      },
    ],
  },
  {
    group: 'Daily Digest',
    items: [
      {
        id: 'digest-get',
        method: 'GET',
        path: '/api/v1/daily-digest',
        role: 'READONLY',
        summary: 'Retrieve the daily digest for a given date',
        params: [
          { name: 'date', type: 'string', required: true, desc: 'ISO date (YYYY-MM-DD).' },
        ],
        request: '',
        response: `{
  "content": "Today's AI-generated digest...",
  "createdOn": "2025-05-18T19:00:00Z"
}`,
        notes: 'Returns 404 if no digest exists for the given date.',
      },
      {
        id: 'digest-trigger',
        method: 'POST',
        path: '/api/v1/daily-digest',
        role: 'ADMIN',
        summary: 'Trigger digest generation asynchronously',
        params: [
          { name: 'hours', type: 'number', required: false, desc: 'Hours of news to include (default: 24).' },
          { name: 'postToDiscord', type: 'boolean', required: false, desc: 'Post result to Discord (default: true).' },
          { name: 'persist', type: 'boolean', required: false, desc: 'Save to DB (default: false).' },
        ],
        request: '',
        response: '"Digest generation started"',
        notes: 'All params are query parameters. Generation runs in a background thread.',
      },
    ],
  },
  {
    group: 'Feed Item Queue',
    items: [
      {
        id: 'fitp-next',
        method: 'GET',
        path: '/api/v1/feed-item-to-process/next',
        role: 'ADMIN',
        summary: 'Dequeue the next feed item for scraping (marks it in-process)',
        params: [],
        request: '',
        response: 'FeedItemToProcessDto or 404',
        notes: 'Used by the scraper service. Atomically marks the item as in-process.',
      },
      {
        id: 'fitp-has-next',
        method: 'GET',
        path: '/api/v1/feed-item-to-process/has-next',
        role: 'ADMIN',
        summary: 'Check if any unprocessed items are queued',
        params: [],
        request: '',
        response: '1 (has items) or 0 (empty)',
        notes: '',
      },
      {
        id: 'fitp-create',
        method: 'POST',
        path: '/api/v1/feed-item-to-process',
        role: 'ADMIN',
        summary: 'Enqueue a feed item for processing',
        params: [],
        request: `{
  "feedId": 1,
  "refId": "unique-ref-string",
  "url": "https://article-url.com",
  "title": "Article title"
}`,
        response: 'FeedItemToProcessDto',
        notes: '',
      },
      {
        id: 'fitp-filter',
        method: 'POST',
        path: '/api/v1/feed-item-to-process/filter',
        role: 'ADMIN',
        summary: 'Filter: return refIds that are NOT yet in the queue',
        params: [],
        request: `{
  "refIds": ["ref1", "ref2", "ref3"]
}`,
        response: '["ref1", "ref3"]  // refIds not found in queue',
        notes: 'Used by scraper to avoid re-queuing already-known items.',
      },
      {
        id: 'fitp-patch',
        method: 'PATCH',
        path: '/api/v1/feed-item-to-process/{id}',
        role: 'ADMIN',
        summary: 'Update the process state of a queued item',
        params: [
          { name: 'id', type: 'path', required: true, desc: 'FeedItemToProcess ID' },
        ],
        request: `{
  "processState": "done"
}`,
        response: 'FeedItemToProcessDto',
        notes: '',
      },
    ],
  },
  {
    group: 'User',
    items: [
      {
        id: 'user-voted',
        method: 'GET',
        path: '/api/v1/user/{id}/voted-news',
        role: 'USER',
        summary: "Get a user's voted news article IDs",
        params: [
          { name: 'id', type: 'path', required: true, desc: 'User identifier' },
          { name: 'date', type: 'string', required: false, desc: 'ISO date filter' },
          { name: 'hours', type: 'string', required: false, desc: 'Hours window filter' },
          { name: 'max', type: 'string', required: false, desc: 'Max results' },
        ],
        request: '',
        response: '["42", "17", "99"]',
        notes: 'Requires authentication; only returns votes by the authenticated user.',
      },
    ],
  },
  {
    group: 'RSS Feed',
    items: [
      {
        id: 'rss',
        method: 'GET',
        path: '/rss',
        role: 'public',
        summary: 'Serve an RSS feed of processed news articles',
        params: [
          { name: 'feedIds', type: 'number[]', required: false, desc: 'Filter to specific feed IDs' },
          { name: 'includeTags', type: 'string[]', required: false, desc: 'Only include articles with these tags' },
          { name: 'excludeTags', type: 'string[]', required: false, desc: 'Exclude articles with these tags' },
        ],
        request: '',
        response: 'RSS/XML document',
        notes: 'Example: /rss?feedIds=1,2&includeTags=tech&excludeTags=ads',
      },
    ],
  },
]

const dtos = [
  {
    name: 'AuthenticateUserDto',
    direction: 'Request',
    description: 'Credentials for login and register',
    fields: [
      { name: 'email', type: 'string', notes: 'User email address' },
      { name: 'password', type: 'string', notes: 'Plain-text password' },
    ],
  },
  {
    name: 'AuthResponse',
    direction: 'Response',
    description: 'Returned by POST /auth/login',
    fields: [
      { name: 'authToken', type: 'string', notes: 'Token used as password in Basic Auth' },
    ],
  },
  {
    name: 'NewsDto',
    direction: 'Response',
    description: 'Represents a processed news article',
    fields: [
      { name: 'id', type: 'Long', notes: '' },
      { name: 'feedId', type: 'Long', notes: 'Source feed ID' },
      { name: 'createdOn', type: 'Instant', notes: 'ISO-8601 timestamp' },
      { name: 'url', type: 'string', notes: 'Original article URL' },
      { name: 'text', type: 'string', notes: 'AI-generated summary' },
      { name: 'title', type: 'string', notes: 'Article title' },
      { name: 'advertising', type: 'boolean', notes: 'Flagged as advertising' },
      { name: 'tags', type: 'string[]', notes: 'AI-assigned tags' },
      { name: 'timely', type: 'boolean', notes: 'Time-sensitive content' },
      { name: 'impactScope', type: 'string', notes: 'global | international | europa | deutschland | regional | branche' },
      { name: 'voted', type: 'boolean', notes: 'Whether current user voted on this' },
    ],
  },
  {
    name: 'CreateNewsDto',
    direction: 'Request',
    description: 'Payload for POST /news (scraper use)',
    fields: [
      { name: 'feedId', type: 'Long', notes: '' },
      { name: 'originalFeedItemId', type: 'Long', notes: 'FeedItemToProcess ID' },
      { name: 'text', type: 'string', notes: 'AI summary' },
      { name: 'advertising', type: 'boolean', notes: '' },
      { name: 'tags', type: 'string[]', notes: '' },
      { name: 'timely', type: 'boolean', notes: '' },
      { name: 'impactScope', type: 'string', notes: 'Validated enum value' },
    ],
  },
  {
    name: 'PatchNewsDto',
    direction: 'Request',
    description: 'Payload for PATCH /news/{id}',
    fields: [
      { name: 'tagsToAdd', type: 'string[]', notes: 'Tags to append to existing tags' },
    ],
  },
  {
    name: 'VoteDto',
    direction: 'Request',
    description: 'Payload for POST /news/{id}/vote',
    fields: [
      { name: 'vote', type: 'boolean', notes: 'true = upvote, false = downvote' },
    ],
  },
  {
    name: 'FeedDto',
    direction: 'Response',
    description: 'RSS feed source',
    fields: [
      { name: 'id', type: 'Long', notes: '' },
      { name: 'url', type: 'string', notes: 'RSS feed URL' },
      { name: 'title', type: 'string', notes: 'Feed display name' },
      { name: 'cookie', type: 'string', notes: 'Auth cookie for gated feeds (nullable)' },
      { name: 'createdOn', type: 'Instant', notes: 'ISO-8601 timestamp' },
    ],
  },
  {
    name: 'PatchFeedDto',
    direction: 'Request',
    description: 'Payload for PATCH /feed/{id}',
    fields: [
      { name: 'cookie', type: 'string', notes: 'Cookie string for authenticated feeds' },
    ],
  },
  {
    name: 'FeedItemToProcessDto',
    direction: 'Response',
    description: 'Queued item awaiting scraping',
    fields: [
      { name: 'id', type: 'Long', notes: '' },
      { name: 'feed', type: 'FeedDto', notes: 'Parent feed' },
      { name: 'refId', type: 'string', notes: 'Unique reference from RSS' },
      { name: 'url', type: 'string', notes: 'Article URL' },
      { name: 'title', type: 'string', notes: 'Article title' },
      { name: 'processState', type: 'string', notes: 'Current state (e.g. queued, in-process, done)' },
      { name: 'createdOn', type: 'string', notes: 'ISO-8601 string' },
      { name: 'updatedOn', type: 'string', notes: 'ISO-8601 string' },
    ],
  },
  {
    name: 'CreateFeedItemToProcessDto',
    direction: 'Request',
    description: 'Enqueue a new item for scraping',
    fields: [
      { name: 'feedId', type: 'Long', notes: '' },
      { name: 'refId', type: 'string', notes: 'Unique RSS item reference' },
      { name: 'url', type: 'string', notes: '' },
      { name: 'title', type: 'string', notes: '' },
    ],
  },
  {
    name: 'DailyDigestDto',
    direction: 'Response',
    description: 'AI-generated daily news digest',
    fields: [
      { name: 'content', type: 'string', notes: 'Full digest text' },
      { name: 'createdOn', type: 'Instant', notes: 'ISO-8601 timestamp' },
    ],
  },
  {
    name: 'CreateTagGroupDto',
    direction: 'Request',
    description: 'Update tag group mapping (PATCH /tag-group)',
    fields: [
      { name: 'tags', type: 'Map<string, string[]>', notes: 'Group name → array of tag strings' },
    ],
  },
]
</script>

<template>
  <div class="dev-page">
    <div class="dev-header">
      <div class="dev-header-text">
        <h1 class="dev-title">Developer Reference</h1>
        <p class="dev-subtitle">REST API · Authentication · Data Structures</p>
      </div>
      <div class="dev-meta">
        <span class="meta-badge">Base URL</span>
        <code class="meta-url">{{ BASE_URL }}</code>
        <button
          class="copy-btn"
          :class="{ copied: copiedId === 'base-url' }"
          @click="copy(BASE_URL, 'base-url')"
        >
          {{ copiedId === 'base-url' ? '✓' : 'copy' }}
        </button>
      </div>
    </div>

    <div class="nav-shell">
      <div class="section-toggle" role="tablist" aria-label="Documentation section">
        <button
          type="button"
          class="section-btn"
          :class="{ active: activeSection === 'concepts' }"
          role="tab"
          :aria-selected="activeSection === 'concepts'"
          @click="selectSection('concepts')"
        >
          <span class="section-btn-label">Concepts</span>
          <span class="section-btn-count">{{ conceptTabs.length }}</span>
        </button>
        <button
          type="button"
          class="section-btn"
          :class="{ active: activeSection === 'reference' }"
          role="tab"
          :aria-selected="activeSection === 'reference'"
          @click="selectSection('reference')"
        >
          <span class="section-btn-label">API Reference</span>
          <span class="section-btn-count">{{ referenceTabs.length }}</span>
        </button>
      </div>

      <div v-if="activeSection === 'concepts'" class="pill-nav">
        <button
          v-for="t in conceptTabs"
          :key="t.id"
          type="button"
          class="pill"
          :class="{ active: activeTab === t.id }"
          @click="activeTab = t.id"
        >
          <span class="pill-kicker">{{ t.kicker }}</span>
          <span class="pill-label">{{ t.label }}</span>
        </button>
      </div>

      <div v-else class="pill-nav">
        <button
          v-for="t in referenceTabs"
          :key="t.id"
          type="button"
          class="pill pill-ref"
          :class="{ active: activeTab === t.id }"
          @click="activeTab = t.id"
        >
          <span class="pill-label">{{ t.label }}</span>
        </button>
      </div>
    </div>

    <!-- ARCHITECTURE TAB -->
    <div v-if="activeTab === 'architecture'" class="tab-content">
      <div class="concept-hero">
        <div class="concept-kicker">01 — Architecture</div>
        <h2 class="concept-title">Five workloads, one pipeline</h2>
        <p class="concept-lede">
          In production each box below is its own Kubernetes deployment. The
          <strong>backend</strong> owns state and scheduling, the
          <strong>scraper</strong> turns articles into AI summaries, the
          <strong>taggroupper</strong> sorts those tags into categories, the
          <strong>frontend</strong> reads, and <strong>auth</strong> issues sessions. All
          coordination happens over HTTP against the backend.
        </p>
      </div>

      <div class="svc-grid">
        <div v-for="s in services" :key="s.name" class="svc-card" :class="s.accent">
          <div class="svc-card-head">
            <span class="svc-glyph">{{ s.glyph }}</span>
            <div class="svc-card-titles">
              <div class="svc-name">{{ s.name }}</div>
              <div class="svc-stack">{{ s.stack }}</div>
            </div>
            <span class="svc-port">:{{ s.port }}</span>
          </div>
          <div class="svc-role">{{ s.role }}</div>
          <div v-if="s.cmd" class="svc-cmd">
            <span class="svc-cmd-label">cmd</span>
            <code>{{ s.cmd }}</code>
          </div>
          <div v-if="s.note" class="svc-note">{{ s.note }}</div>
        </div>
      </div>

      <section class="section">
        <h3 class="sub-title">Data flow</h3>
        <div class="flow-diagram">
          <div class="flow-row">
            <div class="flow-node flow-source">RSS sources</div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-svc">news-backend<br /><small>schedule + fetch</small></div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-db">feed_item_to_process</div>
          </div>
          <div class="flow-row">
            <div class="flow-node flow-svc">news-scraper<br /><small>poll · scrape · AI</small></div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-db">news + tags</div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-svc">news-frontend<br /><small>reader UI</small></div>
          </div>
          <div class="flow-row">
            <div class="flow-node flow-svc">news-taggroupper<br /><small>every 10 min</small></div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-db">tag_group<br /><small>categorized facets</small></div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-svc">news-frontend<br /><small>filter UI</small></div>
          </div>
          <div class="flow-row">
            <div class="flow-node flow-svc">news-auth<br /><small>basic auth → token</small></div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-db">Redis<br /><small>session store</small></div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-svc">cookie on every call</div>
          </div>
        </div>
        <p class="concept-note">
          The queue and the news/tag rows are the seams. Nothing about the AI engine leaks
          into the backend; nothing about HTML rendering leaks into the scrapers. Workloads
          coordinate only through database state surfaced as REST.
        </p>
      </section>

      <section class="section">
        <h3 class="sub-title">Runtime check</h3>
        <div class="code-block">
          <pre>$ kubectl get pods -n news
NAME                                READY   STATUS
news-auth-9d45575cc-…               1/1     Running
news-backend-f79857ff6-…            1/1     Running
news-frontend-bc55db7c9-…           1/1     Running
news-scraper-8565947887-…           1/1     Running
news-taggroupper-765bfb594b-…       1/1     Running</pre>
        </div>
      </section>
    </div>

    <!-- PIPELINE TAB -->
    <div v-if="activeTab === 'pipeline'" class="tab-content">
      <div class="concept-hero">
        <div class="concept-kicker">02 — Ingestion</div>
        <h2 class="concept-title">From RSS to a row in <code class="hero-code">news</code></h2>
        <p class="concept-lede">
          The backend pulls RSS on a schedule and parks each unseen item in a state-machine
          table. Scrapers dequeue atomically, download the article, run the AI engine, and
          POST the result back. Failure has a budget.
        </p>
      </div>

      <section class="section">
        <h3 class="sub-title">Process state machine</h3>
        <div class="state-flow">
          <div v-for="(s, i) in pipelineStates" :key="s.id" class="state-step" :class="s.tone">
            <div class="state-num">{{ i + 1 }}</div>
            <div class="state-body">
              <div class="state-id">{{ s.id }}</div>
              <div class="state-desc">{{ s.desc }}</div>
            </div>
          </div>
        </div>
        <p class="concept-note">
          A row that fails 5 times is moved to <code>FAILED</code> and ignored. This stops a
          single broken URL from blocking the queue indefinitely.
        </p>
      </section>

      <section class="section">
        <h3 class="sub-title">Where to look</h3>
        <div class="file-list">
          <div class="file-row">
            <code class="file-path">news-backend/.../service/FeedItemToProcessService.java</code>
            <span class="file-tag">queue ops</span>
          </div>
          <div class="file-row">
            <code class="file-path">news-backend/.../db/FeedItemToProcess.java</code>
            <span class="file-tag">entity</span>
          </div>
          <div class="file-row">
            <code class="file-path">scraper/main.py</code>
            <span class="file-tag">poller</span>
          </div>
          <div class="file-row">
            <code class="file-path">scraper/fetch_atom.py</code>
            <span class="file-tag">RSS pull</span>
          </div>
        </div>
      </section>

      <section class="section">
        <h3 class="sub-title">Endpoints involved</h3>
        <ul class="endpoint-mini-list">
          <li><span class="method-badge method-get">GET</span> <code>/api/v1/feed-item-to-process/next</code> — atomic dequeue</li>
          <li><span class="method-badge method-post">POST</span> <code>/api/v1/feed-item-to-process</code> — enqueue</li>
          <li><span class="method-badge method-post">POST</span> <code>/api/v1/feed-item-to-process/filter</code> — dedupe by refId</li>
          <li><span class="method-badge method-patch">PATCH</span> <code>/api/v1/feed-item-to-process/{id}</code> — state transition</li>
          <li><span class="method-badge method-post">POST</span> <code>/api/v1/news</code> — write the result</li>
        </ul>
      </section>
    </div>

    <!-- AI TAB -->
    <div v-if="activeTab === 'ai'" class="tab-content">
      <div class="concept-hero">
        <div class="concept-kicker">03 — AI Engines</div>
        <h2 class="concept-title">Pluggable engines, schema-locked output</h2>
        <p class="concept-lede">
          AI calls live in the scraper, not the backend. The active engine is chosen by env
          var, the prompts are local, and every model response is constrained by a JSON
          schema so downstream code can rely on the shape.
        </p>
      </div>

      <div class="ai-flow-grid">
        <div v-for="f in aiEngines" :key="f.name" class="ai-flow-card">
          <div class="ai-flow-head">
            <span class="ai-flow-name">{{ f.name }}</span>
            <code class="ai-flow-file">{{ f.file }}</code>
          </div>
          <p class="ai-flow-desc">{{ f.desc }}</p>
          <div class="code-block">
            <pre>{{ f.schema }}</pre>
          </div>
        </div>
      </div>

      <section class="section">
        <h3 class="sub-title">Configuration</h3>
        <table class="env-table">
          <thead>
            <tr>
              <th>Env var</th>
              <th>Example</th>
              <th>Purpose</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in aiEnvVars" :key="e.key">
              <td><code>{{ e.key }}</code></td>
              <td><code class="env-val">{{ e.val }}</code></td>
              <td>{{ e.note }}</td>
            </tr>
          </tbody>
        </table>
        <p class="concept-note">
          Token budget per call is 118k. The summary call enforces a max of 3 tags and a
          fixed <code>impact_scope</code> enum so the frontend filter UI never sees an
          unexpected value.
        </p>
      </section>
    </div>

    <!-- TAGS TAB -->
    <div v-if="activeTab === 'tags'" class="tab-content">
      <div class="concept-hero">
        <div class="concept-kicker">04 — Tag Groups</div>
        <h2 class="concept-title">Raw tags, regrouped daily</h2>
        <p class="concept-lede">
          The AI summary step emits free-form tags per article. Once a day, a separate
          scraper command sorts every tag from today into a small, fixed set of categories
          so the reader UI can filter by topic without an explosion of facets.
        </p>
      </div>

      <section class="section">
        <h3 class="sub-title">How tags are grouped</h3>
        <div class="flow-diagram">
          <div class="flow-row">
            <div class="flow-node flow-source">
              raw tags<br /><small>GET /tag-group/raw</small>
            </div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-svc">
              news-taggroupper<br /><small>./run.sh taggroups</small>
            </div>
            <span class="flow-arrow">→</span>
            <div class="flow-node flow-db">
              tag_group<br /><small>PATCH /tag-group</small>
            </div>
          </div>
        </div>
        <p class="concept-note">
          The taggroupper is its own Kubernetes deployment, using the same image as
          <code>news-scraper</code> but launched with the <code>taggroups</code> argument.
          Splitting it keeps the latency-sensitive article scraper from sharing a process
          with the periodic regrouping job.
        </p>
      </section>

      <section class="section">
        <h3 class="sub-title">Fixed categories</h3>
        <div class="chip-row">
          <span v-for="c in tagCategories" :key="c" class="chip">{{ c }}</span>
        </div>
        <p class="concept-note">
          The category list is hard-coded in the prompt. To add or rename one, edit
          <code>scraper/create_tag_groups.py</code>; the next PATCH overwrites the prior
          mapping in full.
        </p>
      </section>

      <section class="section">
        <h3 class="sub-title">Run it locally</h3>
        <div class="code-block">
          <pre>cd scraper
python main.py taggroups   # one-shot regroup</pre>
        </div>
      </section>
    </div>

    <!-- DIGEST TAB -->
    <div v-if="activeTab === 'digest'" class="tab-content">
      <div class="concept-hero">
        <div class="concept-kicker">05 — Daily Digest</div>
        <h2 class="concept-title">One AI summary, every evening</h2>
        <p class="concept-lede">
          At 19:00 Berlin time the backend collects the last 24h of news, asks the model
          for a digest, posts it to Discord, and persists it. Readers can ask for any
          historical digest by date.
        </p>
      </div>

      <div class="digest-grid">
        <div class="digest-card">
          <div class="digest-label">Schedule</div>
          <code class="digest-cron">0 0 19 * * *</code>
          <div class="digest-meta">zone: Europe/Berlin</div>
        </div>
        <div class="digest-card">
          <div class="digest-label">Window</div>
          <code class="digest-cron">24h</code>
          <div class="digest-meta">configurable via <code>hours</code> param</div>
        </div>
        <div class="digest-card">
          <div class="digest-label">Sinks</div>
          <code class="digest-cron">Discord + DB</code>
          <div class="digest-meta">webhook + daily_digest table</div>
        </div>
      </div>

      <section class="section">
        <h3 class="sub-title">Generation flow</h3>
        <div class="state-flow">
          <div class="state-step state-new">
            <div class="state-num">1</div>
            <div class="state-body">
              <div class="state-id">Collect</div>
              <div class="state-desc">Last 24h of news rows, ordered by createdOn.</div>
            </div>
          </div>
          <div class="state-step state-prog">
            <div class="state-num">2</div>
            <div class="state-body">
              <div class="state-id">Summarize</div>
              <div class="state-desc">AiSummarizationService.summarize() — uses the configured model.</div>
            </div>
          </div>
          <div class="state-step state-done">
            <div class="state-num">3</div>
            <div class="state-body">
              <div class="state-id">Post</div>
              <div class="state-desc">DiscordService.postMessage() to every webhook URL.</div>
            </div>
          </div>
          <div class="state-step state-fail">
            <div class="state-num">4</div>
            <div class="state-body">
              <div class="state-id">Persist</div>
              <div class="state-desc">Insert into daily_digest so /api/v1/daily-digest can serve it later.</div>
            </div>
          </div>
        </div>
      </section>

      <section class="section">
        <h3 class="sub-title">Trigger manually</h3>
        <div class="code-block">
          <pre>POST /api/v1/daily-digest?hours=24&amp;postToDiscord=true&amp;persist=true</pre>
        </div>
        <p class="concept-note">
          Generation runs in a background thread — the endpoint returns immediately.
          Requires ADMIN.
        </p>
      </section>
    </div>

    <!-- DATABASE TAB -->
    <div v-if="activeTab === 'database'" class="tab-content">
      <div class="concept-hero">
        <div class="concept-kicker">06 — Database</div>
        <h2 class="concept-title">MariaDB, evolved by Flyway</h2>
        <p class="concept-lede">
          Every schema change is a numbered migration in
          <code>news-backend/src/main/resources/db/migration/</code>. Flyway applies them on
          startup; there is no other source of truth for the schema.
        </p>
      </div>

      <section class="section">
        <h3 class="sub-title">Schema timeline</h3>
        <div class="timeline">
          <div v-for="m in migrations" :key="m.ver" class="timeline-row">
            <div class="timeline-ver">{{ m.ver }}</div>
            <div class="timeline-dot"></div>
            <div class="timeline-what">{{ m.what }}</div>
          </div>
        </div>
      </section>

      <section class="section">
        <h3 class="sub-title">Core tables</h3>
        <div class="table-grid">
          <div class="table-card">
            <div class="table-name">news</div>
            <div class="table-desc">Processed articles with AI summary, tags, impact_scope.</div>
          </div>
          <div class="table-card">
            <div class="table-name">feed_item_to_process</div>
            <div class="table-desc">Queue with NEW → IN_PROGRESS → DONE/ERROR/FAILED state.</div>
          </div>
          <div class="table-card">
            <div class="table-name">feed</div>
            <div class="table-desc">RSS source URL + optional auth cookie.</div>
          </div>
          <div class="table-card">
            <div class="table-name">tag_group · tags</div>
            <div class="table-desc">Many-to-many grouping via tag_group_tags.</div>
          </div>
          <div class="table-card">
            <div class="table-name">users · news_vote</div>
            <div class="table-desc">Auth users and per-article up/down votes.</div>
          </div>
          <div class="table-card">
            <div class="table-name">daily_digest</div>
            <div class="table-desc">One row per day · content LONGTEXT.</div>
          </div>
        </div>
      </section>
    </div>

    <!-- DEPLOYMENT TAB -->
    <div v-if="activeTab === 'deployment'" class="tab-content">
      <div class="concept-hero">
        <div class="concept-kicker">07 — Deployment</div>
        <h2 class="concept-title">Compose for dev, Helm for prod</h2>
        <p class="concept-lede">
          Each service ships its own Dockerfile. <code>compose.yml</code> wires the whole
          stack locally; the Helm chart in <code>helm/deep-digest-rss/</code> drives the
          Kubernetes deploy at news.oglimmer.com.
        </p>
      </div>

      <div class="deploy-grid">
        <div v-for="d in deployTargets" :key="d.name" class="deploy-card">
          <div class="deploy-head">
            <span class="deploy-name">{{ d.name }}</span>
          </div>
          <div class="code-block deploy-cmd">
            <pre>{{ d.cmd }}</pre>
          </div>
          <p class="deploy-desc">{{ d.desc }}</p>
          <div class="deploy-note">{{ d.note }}</div>
        </div>
      </div>

      <section class="section">
        <h3 class="sub-title">Workloads in production</h3>
        <table class="env-table">
          <thead>
            <tr>
              <th>Deployment</th>
              <th>Image</th>
              <th>Base</th>
              <th>Exposes</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><code>news-backend</code></td>
              <td><code>news-backend</code></td>
              <td>JRE 25</td>
              <td>8080</td>
            </tr>
            <tr>
              <td><code>news-frontend</code></td>
              <td><code>news-frontend</code></td>
              <td>Nginx</td>
              <td>80</td>
            </tr>
            <tr>
              <td><code>news-auth</code></td>
              <td><code>news-auth</code></td>
              <td>Node 24</td>
              <td>3000</td>
            </tr>
            <tr>
              <td><code>news-scraper</code></td>
              <td><code>news-scraper</code></td>
              <td>Python 3.11</td>
              <td>— (poller)</td>
            </tr>
            <tr>
              <td><code>news-taggroupper</code></td>
              <td><code>news-scraper</code> *</td>
              <td>Python 3.11</td>
              <td>— (periodic)</td>
            </tr>
          </tbody>
        </table>
        <p class="concept-note">
          * <code>news-taggroupper</code> reuses the scraper image with
          <code>command: ["./run.sh", "taggroups"]</code>. Renovate keeps base images and
          dependencies on a slow ratchet — auto-merge for minor/patch, PRs for majors.
        </p>
      </section>
    </div>

    <!-- OVERVIEW TAB -->
    <div v-if="activeTab === 'overview'" class="tab-content">
      <section class="section">
        <h2 class="section-title">Authentication</h2>
        <p class="section-intro">The API supports three authentication modes. All stateless — no sessions.</p>

        <div class="auth-cards">
          <div class="auth-card">
            <div class="auth-card-header">
              <span class="auth-num">1</span>
              <span class="auth-card-title">Guest (read-only)</span>
            </div>
            <p class="auth-card-desc">Use fixed credentials for unauthenticated read access. No login required.</p>
            <div class="code-block">
              <pre>Authorization: Basic {{ btoa('read:read') }}</pre>
              <button
                class="copy-btn"
                :class="{ copied: copiedId === 'guest' }"
                @click="copy(`Authorization: Basic ${btoa('read:read')}`, 'guest')"
              >{{ copiedId === 'guest' ? '✓' : 'copy' }}</button>
            </div>
            <p class="code-note">Decoded: <code>read:read</code></p>
          </div>

          <div class="auth-card">
            <div class="auth-card-header">
              <span class="auth-num">2</span>
              <span class="auth-card-title">Token Login Flow</span>
            </div>
            <p class="auth-card-desc">Login to get an authToken, then send it as the password in Basic Auth.</p>
            <div class="code-block">
              <pre>// Step 1 — login
POST /api/v1/auth/login
{ "email": "you@example.com", "password": "secret" }
→ { "authToken": "abc123..." }

// Step 2 — use token
Authorization: Basic base64(email:authToken)</pre>
              <button
                class="copy-btn"
                :class="{ copied: copiedId === 'token-flow' }"
                @click="copy('POST /api/v1/auth/login\n{ &quot;email&quot;: &quot;you@example.com&quot;, &quot;password&quot;: &quot;secret&quot; }', 'token-flow')"
              >{{ copiedId === 'token-flow' ? '✓' : 'copy' }}</button>
            </div>
          </div>

          <div class="auth-card">
            <div class="auth-card-header">
              <span class="auth-num">3</span>
              <span class="auth-card-title">Query Parameter</span>
            </div>
            <p class="auth-card-desc">Embed credentials as a Base64-encoded query parameter — useful for RSS clients or direct links.</p>
            <div class="code-block">
              <pre>GET /api/v1/news?date=2025-05-18&amp;auth=BASE64(email:authToken)</pre>
              <button
                class="copy-btn"
                :class="{ copied: copiedId === 'query-auth' }"
                @click="copy('GET /api/v1/news?date=2025-05-18&auth=BASE64(email:authToken)', 'query-auth')"
              >{{ copiedId === 'query-auth' ? '✓' : 'copy' }}</button>
            </div>
            <p class="code-note">Param name: <code>auth</code> · Value: <code>base64(email:authToken)</code></p>
          </div>
        </div>
      </section>

      <section class="section">
        <h2 class="section-title">Role Levels</h2>
        <table class="role-table">
          <thead>
            <tr>
              <th>Role</th>
              <th>Access</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><span class="role-badge role-public">public</span></td>
              <td>Auth endpoints (<code>/api/v1/auth/**</code>) and RSS feed (<code>/rss</code>)</td>
            </tr>
            <tr>
              <td><span class="role-badge role-readonly">READONLY</span></td>
              <td>Read all <code>/api/v1/**</code> endpoints (news, feeds, tags, digest)</td>
            </tr>
            <tr>
              <td><span class="role-badge role-user">USER</span></td>
              <td>Everything READONLY can do + vote on articles</td>
            </tr>
            <tr>
              <td><span class="role-badge role-admin">ADMIN</span></td>
              <td>Full access: create news, manage feeds, trigger digest, Swagger UI, actuator</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="section">
        <h2 class="section-title">Quick Start Examples</h2>

        <h3 class="example-heading">Fetch today's news</h3>
        <div class="code-block">
          <pre>curl -H "Authorization: Basic {{ btoa('read:read') }}" \
  {{ BASE_URL }}/api/v1/news?date=2025-05-18</pre>
          <button
            class="copy-btn"
            :class="{ copied: copiedId === 'ex1' }"
            @click="copy(`curl -H &quot;Authorization: Basic ${btoa('read:read')}&quot; \\\n  ${BASE_URL}/api/v1/news?date=2025-05-18`, 'ex1')"
          >{{ copiedId === 'ex1' ? '✓' : 'copy' }}</button>
        </div>

        <h3 class="example-heading">Login and vote</h3>
        <div class="code-block">
          <pre>// 1. Login
curl -X POST {{ BASE_URL }}/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"secret"}'

// 2. Vote (replace TOKEN and ID)
curl -X POST {{ BASE_URL }}/api/v1/news/42/vote \
  -H "Authorization: Basic $(echo -n 'you@example.com:TOKEN' | base64)" \
  -H "Content-Type: application/json" \
  -d '{"vote":true}'</pre>
          <button
            class="copy-btn"
            :class="{ copied: copiedId === 'ex2' }"
            @click="copy(`curl -X POST ${BASE_URL}/api/v1/auth/login -H 'Content-Type: application/json' -d '{&quot;email&quot;:&quot;you@example.com&quot;,&quot;password&quot;:&quot;secret&quot;}'`, 'ex2')"
          >{{ copiedId === 'ex2' ? '✓' : 'copy' }}</button>
        </div>

        <h3 class="example-heading">Swagger UI</h3>
        <p class="swagger-note">Full interactive API docs are available at <a :href="`${BASE_URL}/swagger-ui/`" target="_blank" rel="noopener">{{ BASE_URL }}/swagger-ui/</a> — requires ADMIN credentials.</p>
      </section>
    </div>

    <!-- ENDPOINTS TAB -->
    <div v-if="activeTab === 'endpoints'" class="tab-content">
      <div v-for="group in endpoints" :key="group.group" class="endpoint-group">
        <h2 class="group-title">{{ group.group }}</h2>
        <div class="endpoint-list">
          <div
            v-for="ep in group.items"
            :key="ep.id"
            class="endpoint-card"
            :class="{ 'endpoint-expanded': expanded === ep.id }"
          >
            <button class="endpoint-summary" @click="toggle(ep.id)">
              <span :class="['method-badge', `method-${ep.method.toLowerCase()}`]">{{ ep.method }}</span>
              <code class="ep-path">{{ ep.path }}</code>
              <span :class="['role-badge', `role-${ep.role.toLowerCase()}`]">{{ ep.role }}</span>
              <span class="ep-summary-text">{{ ep.summary }}</span>
              <span class="ep-chevron">{{ expanded === ep.id ? '▲' : '▼' }}</span>
            </button>

            <div v-if="expanded === ep.id" class="endpoint-detail">
              <div v-if="ep.params.length > 0" class="detail-section">
                <div class="detail-label">Parameters</div>
                <table class="param-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Type / Location</th>
                      <th>Required</th>
                      <th>Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="p in ep.params" :key="p.name">
                      <td><code>{{ p.name }}</code></td>
                      <td>{{ p.type }}</td>
                      <td>{{ p.required ? 'yes' : 'no' }}</td>
                      <td>{{ p.desc }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div v-if="ep.request" class="detail-section">
                <div class="detail-label">Request Body</div>
                <div class="code-block">
                  <pre>{{ ep.request }}</pre>
                  <button
                    class="copy-btn"
                    :class="{ copied: copiedId === ep.id + '-req' }"
                    @click="copy(ep.request, ep.id + '-req')"
                  >{{ copiedId === ep.id + '-req' ? '✓' : 'copy' }}</button>
                </div>
              </div>

              <div v-if="ep.response" class="detail-section">
                <div class="detail-label">Response</div>
                <div class="code-block">
                  <pre>{{ ep.response }}</pre>
                  <button
                    class="copy-btn"
                    :class="{ copied: copiedId === ep.id + '-res' }"
                    @click="copy(ep.response, ep.id + '-res')"
                  >{{ copiedId === ep.id + '-res' ? '✓' : 'copy' }}</button>
                </div>
              </div>

              <div v-if="ep.notes" class="detail-section">
                <div class="detail-note">{{ ep.notes }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- DTOS TAB -->
    <div v-if="activeTab === 'dtos'" class="tab-content">
      <p class="dtos-intro">All DTOs use JSON. Java types map as: <code>Long → number</code>, <code>Instant → ISO-8601 string</code>, <code>boolean → boolean</code>, <code>String[] → string[]</code>.</p>
      <div class="dto-grid">
        <div v-for="dto in dtos" :key="dto.name" class="dto-card">
          <div class="dto-header">
            <span class="dto-name">{{ dto.name }}</span>
            <span :class="['dto-dir', dto.direction === 'Request' ? 'dto-req' : 'dto-res']">
              {{ dto.direction }}
            </span>
          </div>
          <p class="dto-desc">{{ dto.description }}</p>
          <table class="dto-table">
            <thead>
              <tr>
                <th>Field</th>
                <th>Type</th>
                <th>Notes</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="f in dto.fields" :key="f.name">
                <td><code>{{ f.name }}</code></td>
                <td><code class="type-code">{{ f.type }}</code></td>
                <td class="notes-cell">{{ f.notes }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dev-page {
  font-family: var(--font-ui);
  max-width: 900px;
  margin: 0 auto;
}

/* Header */
.dev-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
  border-image: linear-gradient(90deg, var(--accent-line), var(--accent-line) 25%, var(--border-color) 25%) 1;
}

.dev-title {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 400;
  margin: 0 0 0.2rem;
  letter-spacing: -0.02em;
}

.dev-subtitle {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  color: var(--text-muted);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin: 0;
}

.dev-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.meta-badge {
  font-size: 0.6rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-muted);
  font-weight: 600;
}

.meta-url {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.75rem;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  border: 1px solid var(--border-color);
}

/* Nav shell */
.nav-shell {
  margin-bottom: 1.75rem;
}

/* Primary section toggle */
.section-toggle {
  display: inline-flex;
  padding: 0.25rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  gap: 0.15rem;
  margin-bottom: 1rem;
}

.section-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.45rem 0.95rem;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--text-muted);
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s, box-shadow 0.15s;
  letter-spacing: 0.01em;
}

.section-btn:hover {
  color: var(--text-primary);
}

.section-btn.active {
  background: var(--bg-primary);
  color: var(--text-primary);
  box-shadow: 0 1px 2px var(--shadow-color, rgba(0, 0, 0, 0.08));
  font-weight: 600;
}

.section-btn-count {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.6rem;
  font-weight: 700;
  color: var(--text-muted);
  background: var(--bg-tertiary);
  padding: 0.1rem 0.4rem;
  border-radius: 99px;
  min-width: 1.2rem;
  text-align: center;
}

.section-btn.active .section-btn-count {
  color: var(--primary-color);
  background: var(--bg-secondary);
}

/* Secondary pill nav */
.pill-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  padding: 0.5rem 0 0;
  border-top: 1px solid var(--border-color);
}

.pill {
  display: inline-flex;
  align-items: baseline;
  gap: 0.45rem;
  padding: 0.4rem 0.75rem;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  font-family: var(--font-ui);
  font-size: 0.76rem;
  font-weight: 500;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.15s;
  letter-spacing: 0.01em;
  white-space: nowrap;
}

.pill:hover {
  color: var(--text-primary);
  background: var(--bg-secondary);
}

.pill.active {
  color: var(--primary-color);
  background: var(--bg-secondary);
  border-color: var(--border-color);
  font-weight: 600;
}

.pill-kicker {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.58rem;
  font-weight: 700;
  color: var(--text-muted);
  letter-spacing: 0.04em;
  opacity: 0.55;
}

.pill.active .pill-kicker {
  color: var(--primary-color);
  opacity: 1;
}

.pill-ref {
  padding: 0.4rem 0.95rem;
}

/* Concept pages — shared */
.concept-hero {
  margin-bottom: 1.75rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px dashed var(--border-color);
}

.concept-kicker {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.6rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--primary-color);
  margin-bottom: 0.5rem;
}

.concept-title {
  font-family: var(--font-display);
  font-size: 1.6rem;
  font-weight: 400;
  letter-spacing: -0.025em;
  line-height: 1.15;
  margin: 0 0 0.6rem;
}

.hero-code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.9em;
  background: var(--bg-tertiary);
  padding: 0.05em 0.3em;
  border-radius: 4px;
  font-weight: 400;
}

.concept-lede {
  font-size: 0.88rem;
  line-height: 1.65;
  color: var(--text-secondary);
  margin: 0;
  max-width: 60ch;
}

.concept-lede strong {
  color: var(--text-primary);
  font-weight: 600;
}

.concept-note {
  font-size: 0.75rem;
  line-height: 1.6;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border-left: 3px solid var(--accent-line, var(--primary-color));
  padding: 0.6rem 0.85rem;
  border-radius: 0 5px 5px 0;
  margin: 1rem 0 0;
}

.concept-note code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.72rem;
  background: var(--bg-tertiary);
  padding: 0.05em 0.3em;
  border-radius: 3px;
}

.sub-title {
  font-family: var(--font-ui);
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin: 0 0 0.75rem;
  padding-bottom: 0.4rem;
  border-bottom: 1px solid var(--border-color);
}

/* Architecture — service grid */
.svc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 0.75rem;
  margin-bottom: 2rem;
}

.svc-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.85rem;
  border-top: 3px solid var(--primary-color);
  transition: border-color 0.2s;
}

.svc-card.svc-back  { border-top-color: #1565c0; }
.svc-card.svc-front { border-top-color: #2e7d32; }
.svc-card.svc-scrape{ border-top-color: #e65100; }
.svc-card.svc-tag   { border-top-color: #6a1b9a; }
.svc-card.svc-auth  { border-top-color: #880e4f; }

[data-theme="dark"] .svc-card.svc-back  { border-top-color: #64b5f6; }
[data-theme="dark"] .svc-card.svc-front { border-top-color: #66bb6a; }
[data-theme="dark"] .svc-card.svc-scrape{ border-top-color: #ffb74d; }
[data-theme="dark"] .svc-card.svc-tag   { border-top-color: #ba68c8; }
[data-theme="dark"] .svc-card.svc-auth  { border-top-color: #f48fb1; }

.svc-card-head {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.55rem;
}

.svc-glyph {
  width: 1.6rem;
  height: 1.6rem;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-primary);
  flex-shrink: 0;
}

.svc-name {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--text-primary);
}

.svc-stack {
  font-size: 0.65rem;
  color: var(--text-muted);
  letter-spacing: 0.02em;
  margin-top: 0.1rem;
}

.svc-port {
  margin-left: auto;
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.68rem;
  color: var(--text-muted);
}

.svc-role {
  font-size: 0.72rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.svc-card-titles {
  min-width: 0;
  flex: 1;
}

.svc-cmd {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.55rem;
  padding-top: 0.5rem;
  border-top: 1px dashed var(--border-color);
}

.svc-cmd-label {
  font-size: 0.55rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-muted);
  flex-shrink: 0;
}

.svc-cmd code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.68rem;
  color: var(--text-primary);
  background: var(--bg-tertiary);
  padding: 0.1rem 0.4rem;
  border-radius: 3px;
  overflow-x: auto;
  white-space: nowrap;
}

.svc-note {
  font-size: 0.62rem;
  color: var(--text-muted);
  margin-top: 0.4rem;
  line-height: 1.45;
  font-style: italic;
}

/* Flow diagrams */
.flow-diagram {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1rem;
}

.flow-row {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.flow-node {
  flex: 1;
  min-width: 8rem;
  padding: 0.55rem 0.7rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
  line-height: 1.35;
}

.flow-node small {
  display: block;
  font-size: 0.62rem;
  font-weight: 400;
  color: var(--text-muted);
  margin-top: 0.15rem;
}

.flow-source {
  border-color: #1565c0;
  color: #1565c0;
}
[data-theme="dark"] .flow-source {
  border-color: #64b5f6;
  color: #90caf9;
}

.flow-svc {
  background: var(--bg-tertiary);
}

.flow-db {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.68rem;
  border-style: dashed;
}

.flow-arrow {
  font-size: 1rem;
  color: var(--text-muted);
  flex-shrink: 0;
}

/* State flow (pipeline + digest) */
.state-flow {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.state-step {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.65rem 0.85rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-left: 3px solid var(--primary-color);
  border-radius: 6px;
}

.state-step.state-new  { border-left-color: #1565c0; }
.state-step.state-prog { border-left-color: #e65100; }
.state-step.state-done { border-left-color: #2e7d32; }
.state-step.state-err  { border-left-color: #c62828; }
.state-step.state-fail { border-left-color: #6a1b9a; }

[data-theme="dark"] .state-step.state-new  { border-left-color: #64b5f6; }
[data-theme="dark"] .state-step.state-prog { border-left-color: #ffb74d; }
[data-theme="dark"] .state-step.state-done { border-left-color: #66bb6a; }
[data-theme="dark"] .state-step.state-err  { border-left-color: #ef5350; }
[data-theme="dark"] .state-step.state-fail { border-left-color: #ba68c8; }

.state-num {
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  background: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--text-secondary);
  flex-shrink: 0;
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.state-body { flex: 1; min-width: 0; }

.state-id {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.72rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}

.state-desc {
  font-size: 0.72rem;
  color: var(--text-secondary);
  margin-top: 0.1rem;
  line-height: 1.45;
}

/* File list */
.file-list {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.file-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.45rem 0.7rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 5px;
}

.file-path {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.7rem;
  color: var(--text-secondary);
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  white-space: nowrap;
}

.file-tag {
  font-size: 0.58rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-muted);
  background: var(--bg-tertiary);
  padding: 0.15rem 0.4rem;
  border-radius: 3px;
  flex-shrink: 0;
}

/* Endpoint mini list */
.endpoint-mini-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.endpoint-mini-list li {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.74rem;
  color: var(--text-secondary);
  padding: 0.4rem 0.6rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 5px;
}

.endpoint-mini-list code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.72rem;
  color: var(--text-primary);
}

/* AI flow grid */
.ai-flow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 0.85rem;
  margin-bottom: 2rem;
}

.ai-flow-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.85rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.ai-flow-head {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  justify-content: space-between;
  flex-wrap: wrap;
}

.ai-flow-name {
  font-family: var(--font-display);
  font-size: 1rem;
  font-weight: 400;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.ai-flow-file {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.62rem;
  color: var(--text-muted);
  background: var(--bg-tertiary);
  padding: 0.15rem 0.4rem;
  border-radius: 3px;
}

.ai-flow-desc {
  font-size: 0.73rem;
  color: var(--text-secondary);
  line-height: 1.55;
  margin: 0;
}

/* Env table */
.env-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.74rem;
}

.env-table th {
  text-align: left;
  padding: 0.5rem 0.75rem;
  font-size: 0.62rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
}

.env-table td {
  padding: 0.45rem 0.75rem;
  border-bottom: 1px solid var(--border-color);
  vertical-align: top;
  color: var(--text-secondary);
}

.env-table td:first-child code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.7rem;
  background: var(--bg-tertiary);
  padding: 0.1rem 0.35rem;
  border-radius: 3px;
  color: var(--primary-color);
}

.env-val {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.7rem;
  color: var(--text-secondary);
}

/* Chips */
.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.chip {
  font-size: 0.72rem;
  font-weight: 500;
  padding: 0.3rem 0.7rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 99px;
  color: var(--text-secondary);
  letter-spacing: 0.01em;
}

/* Digest grid */
.digest-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.75rem;
  margin-bottom: 2rem;
}

.digest-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.85rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.digest-label {
  font-size: 0.58rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.digest-cron {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--primary-color);
  letter-spacing: 0.01em;
}

.digest-meta {
  font-size: 0.68rem;
  color: var(--text-secondary);
}

.digest-meta code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.65rem;
  background: var(--bg-tertiary);
  padding: 0.05em 0.3em;
  border-radius: 3px;
}

/* Timeline */
.timeline {
  display: flex;
  flex-direction: column;
  position: relative;
}

.timeline-row {
  display: grid;
  grid-template-columns: 4.5rem 1.5rem 1fr;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0;
  position: relative;
}

.timeline-row::before {
  content: '';
  position: absolute;
  left: calc(4.5rem + 0.4rem + 0.7rem);
  top: 0;
  bottom: 0;
  width: 1px;
  background: var(--border-color);
  z-index: 0;
}

.timeline-row:first-child::before { top: 50%; }
.timeline-row:last-child::before { bottom: 50%; }

.timeline-ver {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--text-muted);
  text-align: right;
}

.timeline-dot {
  width: 0.65rem;
  height: 0.65rem;
  border-radius: 50%;
  background: var(--bg-primary);
  border: 2px solid var(--primary-color);
  justify-self: center;
  position: relative;
  z-index: 1;
}

.timeline-what {
  font-size: 0.74rem;
  color: var(--text-secondary);
  line-height: 1.4;
}

/* Table grid (DB tables) */
.table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0.6rem;
}

.table-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 0.65rem 0.8rem;
}

.table-name {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 0.25rem;
}

.table-desc {
  font-size: 0.72rem;
  color: var(--text-secondary);
  line-height: 1.45;
}

/* Deploy grid */
.deploy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 0.85rem;
  margin-bottom: 2rem;
}

.deploy-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.85rem;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

.deploy-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.deploy-name {
  font-family: var(--font-display);
  font-size: 1rem;
  font-weight: 400;
  letter-spacing: -0.01em;
  color: var(--text-primary);
}

.deploy-cmd pre {
  font-size: 0.68rem;
  padding: 0.55rem 0.75rem;
}

.deploy-desc {
  font-size: 0.72rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
}

.deploy-note {
  font-size: 0.62rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-muted);
  border-top: 1px dashed var(--border-color);
  padding-top: 0.5rem;
}

/* Section */
.section {
  margin-bottom: 2.5rem;
}

.section-title {
  font-family: var(--font-display);
  font-size: 1.15rem;
  font-weight: 400;
  margin: 0 0 0.5rem;
  letter-spacing: -0.01em;
}

.section-intro {
  color: var(--text-secondary);
  font-size: 0.82rem;
  margin: 0 0 1.25rem;
}

/* Auth cards */
.auth-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.auth-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1rem;
}

.auth-card-header {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.5rem;
}

.auth-num {
  width: 1.4rem;
  height: 1.4rem;
  background: var(--primary-color);
  color: #fff;
  border-radius: 50%;
  font-size: 0.65rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.auth-card-title {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--text-primary);
}

.auth-card-desc {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin: 0 0 0.75rem;
  line-height: 1.5;
}

.code-note {
  font-size: 0.68rem;
  color: var(--text-muted);
  margin: 0.4rem 0 0;
}

/* Code block */
.code-block {
  position: relative;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  overflow: hidden;
}

.code-block pre {
  font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', monospace;
  font-size: 0.7rem;
  line-height: 1.6;
  margin: 0;
  padding: 0.75rem 3rem 0.75rem 0.875rem;
  color: var(--text-secondary);
  overflow-x: auto;
  white-space: pre;
}

/* Copy button */
.copy-btn {
  padding: 0.2rem 0.5rem;
  font-family: var(--font-ui);
  font-size: 0.62rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: var(--bg-primary);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}

.copy-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
  border-color: var(--border-hover);
}

.copy-btn.copied {
  background: var(--primary-color);
  color: #fff;
  border-color: var(--primary-color);
}

.code-block .copy-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
}

/* Role table */
.role-table,
.param-table,
.dto-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.78rem;
}

.role-table th,
.param-table th,
.dto-table th {
  text-align: left;
  padding: 0.5rem 0.75rem;
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
}

.role-table td,
.param-table td,
.dto-table td {
  padding: 0.5rem 0.75rem;
  border-bottom: 1px solid var(--border-color);
  vertical-align: top;
  line-height: 1.4;
}

.role-table tr:last-child td,
.param-table tr:last-child td,
.dto-table tr:last-child td {
  border-bottom: none;
}

.role-table td code,
.param-table td code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.72rem;
  background: var(--bg-tertiary);
  padding: 0.1rem 0.3rem;
  border-radius: 3px;
}

/* Role badges */
.role-badge {
  display: inline-block;
  padding: 0.12rem 0.45rem;
  border-radius: 3px;
  font-size: 0.62rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  white-space: nowrap;
  flex-shrink: 0;
}

.role-public   { background: #e8f5e9; color: #2e7d32; }
.role-readonly { background: #e3f2fd; color: #1565c0; }
.role-user     { background: #fff3e0; color: #e65100; }
.role-admin    { background: #fce4ec; color: #880e4f; }

[data-theme="dark"] .role-public   { background: #1b3a1f; color: #66bb6a; }
[data-theme="dark"] .role-readonly { background: #0d2a42; color: #64b5f6; }
[data-theme="dark"] .role-user     { background: #3e2000; color: #ffb74d; }
[data-theme="dark"] .role-admin    { background: #3e0017; color: #f48fb1; }

/* Method badges */
.method-badge {
  display: inline-block;
  padding: 0.14rem 0.5rem;
  border-radius: 3px;
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.62rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  white-space: nowrap;
  flex-shrink: 0;
}

.method-get    { background: #e3f2fd; color: #0d47a1; }
.method-post   { background: #e8f5e9; color: #1b5e20; }
.method-patch  { background: #fff8e1; color: #e65100; }
.method-delete { background: #fce4ec; color: #880e4f; }

[data-theme="dark"] .method-get    { background: #0d2a42; color: #90caf9; }
[data-theme="dark"] .method-post   { background: #1b3a1f; color: #a5d6a7; }
[data-theme="dark"] .method-patch  { background: #3e2800; color: #ffcc80; }
[data-theme="dark"] .method-delete { background: #3e0017; color: #f48fb1; }

/* Example section */
.example-heading {
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 1.25rem 0 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.swagger-note {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin: 0.5rem 0;
}

/* Endpoint group */
.endpoint-group {
  margin-bottom: 2rem;
}

.group-title {
  font-family: var(--font-display);
  font-size: 1.05rem;
  font-weight: 400;
  margin: 0 0 0.75rem;
  color: var(--text-secondary);
  letter-spacing: -0.01em;
  padding-bottom: 0.4rem;
  border-bottom: 1px solid var(--border-color);
}

.endpoint-list {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.endpoint-card {
  border: 1px solid var(--border-color);
  border-radius: 7px;
  overflow: hidden;
  transition: border-color 0.2s;
}

.endpoint-card.endpoint-expanded {
  border-color: var(--border-hover);
}

.endpoint-summary {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.6rem 0.875rem;
  background: var(--bg-secondary);
  border: none;
  cursor: pointer;
  text-align: left;
  transition: background-color 0.15s;
  flex-wrap: wrap;
}

.endpoint-summary:hover {
  background: var(--bg-hover);
}

.ep-path {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.75rem;
  color: var(--text-primary);
  flex-shrink: 0;
}

.ep-summary-text {
  font-size: 0.75rem;
  color: var(--text-secondary);
  flex: 1;
  min-width: 0;
}

.ep-chevron {
  font-size: 0.55rem;
  color: var(--text-muted);
  margin-left: auto;
  flex-shrink: 0;
}

.endpoint-detail {
  padding: 1rem 0.875rem;
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.detail-label {
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--text-muted);
  font-weight: 600;
}

.detail-note {
  font-size: 0.75rem;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border-left: 3px solid var(--primary-color);
  padding: 0.5rem 0.75rem;
  border-radius: 0 5px 5px 0;
  line-height: 1.5;
}

/* DTOs */
.dtos-intro {
  font-size: 0.78rem;
  color: var(--text-secondary);
  margin: 0 0 1.5rem;
}

.dtos-intro code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.72rem;
  background: var(--bg-tertiary);
  padding: 0.1rem 0.3rem;
  border-radius: 3px;
}

.dto-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 1rem;
}

.dto-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.dto-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.6rem 0.875rem;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-tertiary);
}

.dto-name {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--text-primary);
}

.dto-dir {
  font-size: 0.6rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  padding: 0.12rem 0.4rem;
  border-radius: 3px;
}

.dto-req { background: #fff3e0; color: #e65100; }
.dto-res { background: #e3f2fd; color: #1565c0; }
[data-theme="dark"] .dto-req { background: #3e2800; color: #ffcc80; }
[data-theme="dark"] .dto-res { background: #0d2a42; color: #90caf9; }

.dto-desc {
  font-size: 0.72rem;
  color: var(--text-secondary);
  margin: 0;
  padding: 0.5rem 0.875rem;
  border-bottom: 1px solid var(--border-color);
}

.dto-table {
  background: var(--bg-secondary);
}

.dto-table th,
.dto-table td {
  padding: 0.4rem 0.75rem;
}

.dto-table td code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.7rem;
}

.type-code {
  color: var(--primary-color);
  background: transparent !important;
}

.notes-cell {
  color: var(--text-muted);
  font-size: 0.72rem;
}

/* Mobile */
@media (max-width: 600px) {
  .dev-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .auth-cards {
    grid-template-columns: 1fr;
  }

  .dto-grid {
    grid-template-columns: 1fr;
  }

  .endpoint-summary {
    gap: 0.4rem;
  }

  .ep-summary-text {
    display: none;
  }

  .concept-title {
    font-size: 1.3rem;
  }

  .concept-lede {
    font-size: 0.82rem;
  }

  .flow-row {
    flex-direction: column;
    align-items: stretch;
  }

  .flow-arrow {
    transform: rotate(90deg);
    text-align: center;
  }

  .timeline-row {
    grid-template-columns: 3.5rem 1rem 1fr;
  }

  .timeline-row::before {
    left: calc(3.5rem + 0.4rem + 0.5rem);
  }
}
</style>
