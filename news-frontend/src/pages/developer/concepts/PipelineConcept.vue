<script setup lang="ts">
import { pipelineStates } from '../content'
</script>

<template>
  <div class="tab-content">
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
</template>
