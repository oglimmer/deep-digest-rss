import { defineStore } from 'pinia'
import type { FeedEntry } from '@/interfaces'
import { fetchFeeds } from '@/api/feeds'

export const useFeedsStore = defineStore('feeds', {
  state: () => ({
    entries: [] as FeedEntry[],
  }),
  actions: {
    async fetch() {
      try {
        this.entries = await fetchFeeds()
      } catch (error) {
        console.error('feeds.fetch failed:', error)
      }
    },
  },
})
