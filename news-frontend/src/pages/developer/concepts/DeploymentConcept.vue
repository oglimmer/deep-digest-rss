<script setup lang="ts">
import { deployTargets } from '../content'
</script>

<template>
  <div class="tab-content">
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
</template>
