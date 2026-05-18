import { defineStore } from 'pinia'
import { fetchTagGroup } from '@/api/tagGroups'
import { useNewsStore } from './news'
import { useFiltersStore } from './filters'
import { useUiStore } from './ui'

export const useTagGroupsStore = defineStore('tagGroups', {
  state: () => ({
    data: {} as Record<string, string[]>,
    keys: [] as string[],
  }),
  getters: {
    counts(state): Record<string, number> {
      const news = useNewsStore()
      const filters = useFiltersStore()
      const result: Record<string, number> = {}
      const scoped =
        filters.selectedFeeds.length > 0
          ? news.entries.filter((entry) => filters.selectedFeeds.includes(entry.feedId))
          : news.entries

      for (const key of state.keys) {
        const tags = state.data[key] || []
        result[key] = scoped.filter((entry) => entry.tags.some((tag) => tags.includes(tag))).length
      }
      return result
    },
  },
  actions: {
    async fetch() {
      const ui = useUiStore()
      try {
        this.data = await fetchTagGroup(ui.dateAsDate)
        this.keys = Object.keys(this.data)
      } catch (error) {
        console.error('tagGroups.fetch failed:', error)
      }
    },
  },
})
