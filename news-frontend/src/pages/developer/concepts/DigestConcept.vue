<script setup lang="ts"></script>

<template>
  <div class="tab-content">
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
</template>
