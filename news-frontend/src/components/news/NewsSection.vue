<script setup lang="ts">
import NewsItem from './NewsItem.vue'
import type { FeedEntry, NewsEntry } from '@/interfaces.ts'

const props = defineProps({
  newsEntries: Array<NewsEntry>,
  sectionHeader: String,
  feedEntries: Array<FeedEntry>,
})

const getFeedTitle = (feedId: number) => {
  const feed = props.feedEntries?.find((feed) => feed.id === feedId)
  return feed ? feed.title : 'Unknown Feed'
}
</script>

<template>
  <section v-if="newsEntries && newsEntries.length > 0" class="news-section">
    <div class="section-header">
      <span class="section-rule"></span>
      <h3>{{ sectionHeader }}</h3>
      <span class="section-count">{{ newsEntries.length }}</span>
      <span class="section-rule section-rule-long"></span>
    </div>
    <div class="news-list">
      <article v-for="(entry, index) in newsEntries" :key="entry.id" class="news-entry" :style="{ animationDelay: `${Math.min(index * 0.04, 0.4)}s` }">
        <NewsItem :entry="entry" :feedTitle="getFeedTitle(entry.feedId)" />
      </article>
    </div>
  </section>
</template>

<style scoped>
.news-section {
  margin-bottom: 0.5rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 2rem 0 1.25rem 0;
}

.section-header h3 {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--text-muted);
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  white-space: nowrap;
}

.section-count {
  font-family: var(--font-ui);
  font-size: 0.6rem;
  font-weight: 500;
  color: var(--text-muted);
  background-color: var(--bg-tertiary);
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.section-rule {
  flex: 0 0 2rem;
  height: 1px;
  background-color: var(--section-rule);
}

.section-rule-long {
  flex: 1;
}

.news-list {
  margin: 0;
  padding: 0;
}

.news-entry {
  margin: 0;
  padding: 0;
  animation: articleFadeIn 0.35s ease both;
}
</style>
