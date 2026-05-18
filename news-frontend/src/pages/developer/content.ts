export const BASE_URL = 'https://news.oglimmer.com'

export interface ServiceCard {
  name: string
  stack: string
  port: string
  role: string
  accent: string
  glyph: string
  image: string
  cmd?: string
  note?: string
}

export const services: ServiceCard[] = [
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

export const pipelineStates = [
  { id: 'NEW', desc: 'Just enqueued by the feed fetcher', tone: 'state-new' },
  { id: 'IN_PROGRESS', desc: 'Scraper has dequeued and is downloading', tone: 'state-prog' },
  { id: 'DONE', desc: 'AI summary stored, news row created', tone: 'state-done' },
  { id: 'ERROR', desc: 'Retryable: re-queued (up to 5 attempts)', tone: 'state-err' },
  { id: 'FAILED', desc: 'Terminal: exceeded retry budget', tone: 'state-fail' },
]

export const aiEngines = [
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

export const aiEnvVars = [
  { key: 'GENERATION_ENGINE', val: 'chatgpt', note: 'Selects the active engine' },
  { key: 'MODEL', val: 'gpt-4o-mini', note: 'OpenAI model id' },
  { key: 'API_KEY', val: 'sk-…', note: 'OpenAI key' },
  { key: 'ANTHROPIC_MODEL', val: 'claude-haiku-4-5-20251001', note: 'Anthropic fallback' },
  { key: 'ANTHROPIC_API_KEY', val: 'sk-ant-…', note: 'Anthropic key' },
]

export const tagCategories = [
  'Technology',
  'Environment',
  'Politics',
  'Economy',
  'Science',
  'Culture',
  'Health',
  'Sports',
]

export const migrations = [
  { ver: 'V1.0.0', what: 'news table — id, title, text, url, ref_id, created_on' },
  { ver: 'V2.0.0', what: 'feed_item table' },
  { ver: 'V3.0.0', what: 'feed cookie auth (gated RSS sources)' },
  { ver: 'V4.0.0', what: 'tag_group · tags · news_tags · tag_group_tags' },
  { ver: 'V6.0.0', what: 'users — email, timezone, votes' },
  { ver: 'V8.0.0', what: 'news_vote (user ↔ article)' },
  { ver: 'V13.0.0', what: 'news.timely + news.impact_scope' },
  { ver: 'V14.0.0', what: 'daily_digest — content LONGTEXT, created_on' },
]

export const deployTargets = [
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

export interface EndpointParam {
  name: string
  type: string
  required: boolean
  desc: string
}

export interface Endpoint {
  id: string
  method: string
  path: string
  role: string
  summary: string
  params: EndpointParam[]
  request: string
  response: string
  notes: string
}

export interface EndpointGroup {
  group: string
  items: Endpoint[]
}

export const endpoints: EndpointGroup[] = [
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
        notes:
          'The returned authToken is used as the password in subsequent Basic Auth calls: Authorization: Basic base64(email:authToken)',
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
        notes:
          'impactScope must be one of: global, international, europa, deutschland, regional, branche',
      },
      {
        id: 'news-vote',
        method: 'POST',
        path: '/api/v1/news/{id}/vote',
        role: 'USER',
        summary: 'Cast a vote on an article',
        params: [{ name: 'id', type: 'path', required: true, desc: 'News article ID' }],
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
        params: [{ name: 'id', type: 'path', required: true, desc: 'Feed item reference ID (string)' }],
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
        params: [{ name: 'id', type: 'path', required: true, desc: 'News article ID' }],
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
        params: [{ name: 'id', type: 'path', required: true, desc: 'Feed ID' }],
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
        params: [{ name: 'date', type: 'string', required: true, desc: 'ISO date (YYYY-MM-DD).' }],
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
        params: [{ name: 'id', type: 'path', required: true, desc: 'FeedItemToProcess ID' }],
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

export interface DtoField {
  name: string
  type: string
  notes: string
}

export interface DtoCard {
  name: string
  direction: 'Request' | 'Response'
  description: string
  fields: DtoField[]
}

export const dtos: DtoCard[] = [
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
    fields: [{ name: 'authToken', type: 'string', notes: 'Token used as password in Basic Auth' }],
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
    fields: [{ name: 'tagsToAdd', type: 'string[]', notes: 'Tags to append to existing tags' }],
  },
  {
    name: 'VoteDto',
    direction: 'Request',
    description: 'Payload for POST /news/{id}/vote',
    fields: [{ name: 'vote', type: 'boolean', notes: 'true = upvote, false = downvote' }],
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
    fields: [{ name: 'cookie', type: 'string', notes: 'Cookie string for authenticated feeds' }],
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
    fields: [{ name: 'tags', type: 'Map<string, string[]>', notes: 'Group name → array of tag strings' }],
  },
]
