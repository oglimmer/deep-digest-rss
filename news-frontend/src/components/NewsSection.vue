<script setup lang="ts">
import { defineProps } from 'vue'
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
  <div v-if="newsEntries && newsEntries.length > 0">
    <h3>{{ sectionHeader }}</h3>
    <ul>
      <li v-for="entry in newsEntries" :key="entry.id">
        <NewsItem :entry="entry" :feedTitle="getFeedTitle(entry.feedId)" />
      </li>
    </ul>
  </div>
</template>
