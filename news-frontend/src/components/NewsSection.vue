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
    <h3>{{ sectionHeader }}</h3>
    <div class="news-list">
      <article v-for="entry in newsEntries" :key="entry.id" class="news-entry">
        <NewsItem :entry="entry" :feedTitle="getFeedTitle(entry.feedId)" />
      </article>
    </div>
  </section>
</template>

<style scoped>
.news-section h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 1.5rem 0 1rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-color);
}

.news-list {
  margin: 0;
  padding: 0;
}

.news-entry {
  margin: 0;
  padding: 0;
}
</style>
