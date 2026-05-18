import { defineStore } from 'pinia'

export const useFiltersStore = defineStore('filters', {
  state: () => ({
    selectedFeeds: [] as number[],
    selectedTagGroups: [] as string[],
    excludedTagGroups: [] as string[],
    excludeAds: false,
  }),
  actions: {
    clear() {
      this.selectedFeeds = []
      this.selectedTagGroups = []
      this.excludedTagGroups = []
      this.excludeAds = false
    },
  },
  persist: true,
})
