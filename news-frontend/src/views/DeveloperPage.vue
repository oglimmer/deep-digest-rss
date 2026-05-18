<script setup lang="ts">
import { ref } from 'vue'

type Tab = 'overview' | 'endpoints' | 'dtos'
const activeTab = ref<Tab>('overview')

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

    <div class="tab-nav">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'overview' }"
        @click="activeTab = 'overview'"
      >Overview</button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'endpoints' }"
        @click="activeTab = 'endpoints'"
      >Endpoints</button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'dtos' }"
        @click="activeTab = 'dtos'"
      >DTOs</button>
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

/* Tabs */
.tab-nav {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.tab-btn {
  padding: 0.5rem 1.1rem;
  font-family: var(--font-ui);
  font-size: 0.78rem;
  font-weight: 500;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color 0.2s, border-color 0.2s;
  letter-spacing: 0.01em;
}

.tab-btn:hover {
  color: var(--text-primary);
}

.tab-btn.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
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
}
</style>
