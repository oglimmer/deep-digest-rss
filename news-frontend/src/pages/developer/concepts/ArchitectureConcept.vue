<script setup lang="ts">
import { services } from '../content'
</script>

<template>
  <div class="tab-content">
    <div class="concept-hero">
      <div class="concept-kicker">01 — Architecture</div>
      <h2 class="concept-title">Four workloads, one pipeline</h2>
      <p class="concept-lede">
        In production each box below is its own Kubernetes deployment. The
        <strong>backend</strong> owns state, scheduling, and authentication
        (session cookies for browsers, <code>X-API-Key</code> for services), the
        <strong>scraper</strong> turns articles into AI summaries, the
        <strong>taggroupper</strong> sorts those tags into categories, and the
        <strong>frontend</strong> reads. All coordination happens over HTTP against
        the backend; Redis backs sessions and login rate limits.
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
          <div class="flow-node flow-svc">browser login<br /><small>POST /auth/login</small></div>
          <span class="flow-arrow">→</span>
          <div class="flow-node flow-db">Redis<br /><small>Spring Session</small></div>
          <span class="flow-arrow">→</span>
          <div class="flow-node flow-svc">DDRSS_SESSION cookie<br /><small>or X-API-Key for services</small></div>
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
news-backend-f79857ff6-…            1/1     Running
news-frontend-bc55db7c9-…           1/1     Running
news-scraper-8565947887-…           1/1     Running
news-taggroupper-765bfb594b-…       1/1     Running</pre>
      </div>
    </section>
  </div>
</template>
