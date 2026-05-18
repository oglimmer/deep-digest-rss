<script setup lang="ts">
import { migrations } from '../content'
</script>

<template>
  <div class="tab-content">
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
</template>
