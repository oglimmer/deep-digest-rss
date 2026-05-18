<script setup lang="ts">
import { tagCategories } from '../content'
</script>

<template>
  <div class="tab-content">
    <div class="concept-hero">
      <div class="concept-kicker">04 — Tag Groups</div>
      <h2 class="concept-title">Raw tags, regrouped daily</h2>
      <p class="concept-lede">
        The AI summary step emits free-form tags per article. Once a day, a separate
        scraper command sorts every tag from today into a small, fixed set of categories
        so the reader UI can filter by topic without an explosion of facets.
      </p>
    </div>

    <section class="section">
      <h3 class="sub-title">How tags are grouped</h3>
      <div class="flow-diagram">
        <div class="flow-row">
          <div class="flow-node flow-source">
            raw tags<br /><small>GET /tag-group/raw</small>
          </div>
          <span class="flow-arrow">→</span>
          <div class="flow-node flow-svc">
            news-taggroupper<br /><small>./run.sh taggroups</small>
          </div>
          <span class="flow-arrow">→</span>
          <div class="flow-node flow-db">
            tag_group<br /><small>PATCH /tag-group</small>
          </div>
        </div>
      </div>
      <p class="concept-note">
        The taggroupper is its own Kubernetes deployment, using the same image as
        <code>news-scraper</code> but launched with the <code>taggroups</code> argument.
        Splitting it keeps the latency-sensitive article scraper from sharing a process
        with the periodic regrouping job.
      </p>
    </section>

    <section class="section">
      <h3 class="sub-title">Fixed categories</h3>
      <div class="chip-row">
        <span v-for="c in tagCategories" :key="c" class="chip">{{ c }}</span>
      </div>
      <p class="concept-note">
        The category list is hard-coded in the prompt. To add or rename one, edit
        <code>scraper/create_tag_groups.py</code>; the next PATCH overwrites the prior
        mapping in full.
      </p>
    </section>

    <section class="section">
      <h3 class="sub-title">Run it locally</h3>
      <div class="code-block">
        <pre>cd scraper
python main.py taggroups   # one-shot regroup</pre>
      </div>
    </section>
  </div>
</template>
