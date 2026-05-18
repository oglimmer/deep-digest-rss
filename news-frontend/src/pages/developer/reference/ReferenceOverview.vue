<script setup lang="ts">
import CopyButton from '../CopyButton.vue'
import { BASE_URL } from '../content'
import { useCopy } from '../useCopy'

const { copy, isCopied } = useCopy()

const SESSION_LOGIN = `POST /api/v1/auth/login
Content-Type: application/json

{ "email": "you@example.com", "password": "secret" }

← 200 OK
   Set-Cookie: DDRSS_SESSION=…; HttpOnly; Secure; SameSite=Lax
   { "email": "you@example.com", "roles": ["USER", "READONLY"] }`

const API_KEY_HEADER = 'X-API-Key: <your-api-key>'

const EX_LOGIN = `# 1. Log in (cookie jar captures DDRSS_SESSION)
curl -c cookies.txt -X POST ${BASE_URL}/api/v1/auth/login \\
  -H "Content-Type: application/json" \\
  -d '{"email":"you@example.com","password":"secret"}'

# 2. Reuse the session
curl -b cookies.txt ${BASE_URL}/api/v1/news?date=2025-05-18

# 3. Vote (cookie carries USER role)
curl -b cookies.txt -X POST ${BASE_URL}/api/v1/news/42/vote \\
  -H "Content-Type: application/json" \\
  -d '{"vote":true}'`

const EX_API_KEY = `# Service-to-service: send X-API-Key on every call
curl -H "X-API-Key: $SCRAPER_API_KEY" \\
  ${BASE_URL}/api/v1/feed-item-to-process/next`
</script>

<template>
  <div class="tab-content">
    <section class="section">
      <h2 class="section-title">Authentication</h2>
      <p class="section-intro">
        Two authentication modes: a Redis-backed session cookie for browsers, and a static
        API key for service-to-service calls. Anonymous access is disabled — every API
        request must present one or the other.
      </p>

      <div class="auth-cards">
        <div class="auth-card">
          <div class="auth-card-header">
            <span class="auth-num">1</span>
            <span class="auth-card-title">Session Cookie (browser)</span>
          </div>
          <p class="auth-card-desc">
            <code>POST /api/v1/auth/login</code> returns a <code>DDRSS_SESSION</code> cookie
            backed by Redis (Spring Session). Send the cookie on subsequent calls; absolute
            timeout is 90 days. Logins are rate-limited per email (5 / 15&nbsp;min) and per
            IP (20 / 15&nbsp;min) — both return 429 with <code>Retry-After: 60</code>.
          </p>
          <div class="code-block">
            <pre>{{ SESSION_LOGIN }}</pre>
            <CopyButton :text="SESSION_LOGIN" id="session-login" :copied="isCopied('session-login')" @copy="copy" />
          </div>
          <p class="code-note">
            CSRF: when <code>app.security.csrf-enabled=true</code>, mutating requests must
            echo the <code>XSRF-TOKEN</code> cookie back in an <code>X-XSRF-TOKEN</code>
            header. The SPA does this automatically.
          </p>
        </div>

        <div class="auth-card">
          <div class="auth-card-header">
            <span class="auth-num">2</span>
            <span class="auth-card-title">API Key (service)</span>
          </div>
          <p class="auth-card-desc">
            Stateless callers send <code>X-API-Key</code>. Keys live in the
            <code>api_keys</code> table (BCrypt-hashed) and each key is bound to a set of
            roles. The shipped <code>scraper</code> key has ADMIN/USER/READONLY; its plain
            value comes from the <code>SCRAPER_API_KEY</code> env var and is hashed into
            the DB on backend startup.
          </p>
          <div class="code-block">
            <pre>{{ API_KEY_HEADER }}</pre>
            <CopyButton :text="API_KEY_HEADER" id="api-key" :copied="isCopied('api-key')" @copy="copy" />
          </div>
          <p class="code-note">No cookie, no session — every request is independently authenticated.</p>
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
            <td><code>/api/v1/auth/login</code>, <code>/api/v1/auth/register</code>, <code>/rss</code>, <code>/actuator/health/{liveness,readiness}</code></td>
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
            <td>Full access: create news, manage feeds + queue, trigger digest, Swagger UI, actuator</td>
          </tr>
        </tbody>
      </table>
      <p class="code-note">
        Seeded service accounts (login via <code>/auth/login</code>): <code>read</code> (READONLY),
        <code>write</code> / <code>actuator</code> / <code>swagger</code> (ADMIN + USER + READONLY).
        Their passwords are written from the <code>AUTH_*_PASSWORD</code> env vars on backend startup.
      </p>
    </section>

    <section class="section">
      <h2 class="section-title">Quick Start Examples</h2>

      <h3 class="example-heading">Browser session — login, read, vote</h3>
      <div class="code-block">
        <pre>{{ EX_LOGIN }}</pre>
        <CopyButton :text="EX_LOGIN" id="ex-login" :copied="isCopied('ex-login')" @copy="copy" />
      </div>

      <h3 class="example-heading">Service caller — API key</h3>
      <div class="code-block">
        <pre>{{ EX_API_KEY }}</pre>
        <CopyButton :text="EX_API_KEY" id="ex-api-key" :copied="isCopied('ex-api-key')" @copy="copy" />
      </div>

      <h3 class="example-heading">Swagger UI</h3>
      <p class="swagger-note">
        Full interactive API docs are available at
        <a :href="`${BASE_URL}/swagger-ui/`" target="_blank" rel="noopener">{{ BASE_URL }}/swagger-ui/</a>
        — requires ADMIN (log in as <code>swagger</code> or use an ADMIN API key).
      </p>
    </section>
  </div>
</template>
