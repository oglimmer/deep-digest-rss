import { defineStore } from 'pinia'
import type { DailyDigestEntry } from '@/interfaces'
import { fetchDailyDigest } from '@/api/digest'
import { useUiStore } from './ui'

export const useDigestStore = defineStore('digest', {
  state: () => ({
    current: null as DailyDigestEntry | null,
  }),
  actions: {
    async fetch() {
      const ui = useUiStore()
      try {
        this.current = await fetchDailyDigest(ui.dateAsDate)
      } catch (error) {
        console.error('digest.fetch failed:', error)
        this.current = null
      }
    },
  },
})
