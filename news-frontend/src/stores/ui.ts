import { defineStore } from 'pinia'
import { useNewsStore } from './news'
import { useTagGroupsStore } from './tagGroups'

type DateTuple = [number, number, number]

const todayTuple = (): DateTuple => {
  const now = new Date()
  return [now.getFullYear(), now.getMonth(), now.getDate()]
}

export const useUiStore = defineStore('ui', {
  state: () => ({
    dateToShow: todayTuple(),
    singleNewsMode: false,
  }),
  getters: {
    dateAsDate(state): Date {
      return new Date(state.dateToShow[0], state.dateToShow[1], state.dateToShow[2])
    },
    isDateToday(state): boolean {
      const today = new Date()
      const dts = new Date(state.dateToShow[0], state.dateToShow[1], state.dateToShow[2])
      return (
        dts.getDate() === today.getDate() &&
        dts.getMonth() === today.getMonth() &&
        dts.getFullYear() === today.getFullYear()
      )
    },
  },
  actions: {
    async changeDate(days: number) {
      if (days === 0) {
        this.dateToShow = todayTuple()
      } else {
        const d = new Date(this.dateToShow[0], this.dateToShow[1], this.dateToShow[2])
        d.setDate(d.getDate() + days)
        this.dateToShow = [d.getFullYear(), d.getMonth(), d.getDate()]
      }
      const news = useNewsStore()
      const tagGroups = useTagGroupsStore()
      await news.fetch()
      await tagGroups.fetch()
    },
    toggleSingleNewsMode() {
      this.singleNewsMode = !this.singleNewsMode
      if (!this.singleNewsMode) {
        const news = useNewsStore()
        news.clearDeepLink()
      }
    },
  },
  persist: true,
})
