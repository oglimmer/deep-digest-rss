import { defineStore } from 'pinia'
import type { NewsEntry } from '@/interfaces'
import { fetchNews, fetchNewsById, vote as apiVote } from '@/api/news'
import { useFiltersStore } from './filters'
import { useTagGroupsStore } from './tagGroups'
import { useUiStore } from './ui'

export const useNewsStore = defineStore('news', {
  state: () => ({
    entries: [] as NewsEntry[],
    deepLinkedId: null as number | null,
    deepLinkedEntry: null as NewsEntry | null,
  }),
  getters: {
    filtered(state): NewsEntry[] {
      const filters = useFiltersStore()
      const tagGroups = useTagGroupsStore()

      let result = state.entries.filter(
        (entry) => !(filters.excludeAds && entry.advertising),
      )

      if (filters.selectedFeeds.length > 0) {
        result = result.filter((entry) => filters.selectedFeeds.includes(entry.feedId))
      }

      if (filters.selectedTagGroups.length > 0) {
        const allowed = new Set<string>()
        filters.selectedTagGroups.forEach((group) => {
          ;(tagGroups.data[group] || []).forEach((tag) => allowed.add(tag))
        })
        result = result.filter((entry) => entry.tags.some((tag) => allowed.has(tag)))
      }

      if (filters.excludedTagGroups.length > 0) {
        const excluded = new Set<string>()
        filters.excludedTagGroups.forEach((group) => {
          ;(tagGroups.data[group] || []).forEach((tag) => excluded.add(tag))
        })
        result = result.filter((entry) => !entry.tags.some((tag) => excluded.has(tag)))
      }

      return result
    },
    morningNews(): NewsEntry[] {
      return this.filtered.filter((entry: NewsEntry) => {
        const hour = new Date(entry.createdOn).getHours()
        return hour >= 0 && hour < 12
      })
    },
    afternoonNews(): NewsEntry[] {
      return this.filtered.filter((entry: NewsEntry) => {
        const hour = new Date(entry.createdOn).getHours()
        return hour >= 12 && hour < 18
      })
    },
    nightNews(): NewsEntry[] {
      return this.filtered.filter((entry: NewsEntry) => {
        const hour = new Date(entry.createdOn).getHours()
        return hour >= 18 && hour < 24
      })
    },
  },
  actions: {
    async fetch() {
      const ui = useUiStore()
      try {
        this.entries = await fetchNews(ui.dateAsDate)
      } catch (error) {
        console.error('news.fetch failed:', error)
      }
    },
    async fetchById(id: number) {
      try {
        this.deepLinkedEntry = await fetchNewsById(id)
      } catch (error) {
        console.error('news.fetchById failed:', error)
      }
    },
    async addVote(newsId: number, up: boolean) {
      try {
        await apiVote(newsId, up)
      } catch (error) {
        console.error('news.addVote failed:', error)
        return
      }
      this.entries = this.entries.map((entry) => {
        if (entry.id === newsId) {
          entry.voted = up
        }
        return entry
      })
    },
    clearDeepLink() {
      this.deepLinkedId = null
      this.deepLinkedEntry = null
    },
  },
})
