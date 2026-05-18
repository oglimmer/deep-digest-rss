<script setup lang="ts">
import CopyButton from '../CopyButton.vue'
import { BASE_URL } from '../content'
import { useCopy } from '../useCopy'

const { copy, isCopied } = useCopy()

const btoa = (s: string) => window.btoa(s)
const GUEST_HEADER = `Authorization: Basic ${btoa('read:read')}`
const TOKEN_FLOW = 'POST /api/v1/auth/login\n{ "email": "you@example.com", "password": "secret" }'
const QUERY_AUTH = 'GET /api/v1/news?date=2025-05-18&auth=BASE64(email:authToken)'
const EX1 = `curl -H "Authorization: Basic ${btoa('read:read')}" \\\n  ${BASE_URL}/api/v1/news?date=2025-05-18`
const EX2 = `curl -X POST ${BASE_URL}/api/v1/auth/login -H 'Content-Type: application/json' -d '{"email":"you@example.com","password":"secret"}'`
</script>

<template>
  <div class="tab-content">
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
            <pre>{{ GUEST_HEADER }}</pre>
            <CopyButton :text="GUEST_HEADER" id="guest" :copied="isCopied('guest')" @copy="copy" />
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
            <CopyButton :text="TOKEN_FLOW" id="token-flow" :copied="isCopied('token-flow')" @copy="copy" />
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
            <CopyButton :text="QUERY_AUTH" id="query-auth" :copied="isCopied('query-auth')" @copy="copy" />
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
        <CopyButton :text="EX1" id="ex1" :copied="isCopied('ex1')" @copy="copy" />
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
        <CopyButton :text="EX2" id="ex2" :copied="isCopied('ex2')" @copy="copy" />
      </div>

      <h3 class="example-heading">Swagger UI</h3>
      <p class="swagger-note">Full interactive API docs are available at <a :href="`${BASE_URL}/swagger-ui/`" target="_blank" rel="noopener">{{ BASE_URL }}/swagger-ui/</a> — requires ADMIN credentials.</p>
    </section>
  </div>
</template>
