<script setup lang="ts">
import { aiEngines, aiEnvVars } from '../content'
</script>

<template>
  <div class="tab-content">
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
</template>
