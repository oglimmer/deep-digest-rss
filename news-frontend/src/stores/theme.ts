import { defineStore } from 'pinia'

export type FontFamily = 'system' | 'georgia' | 'palatino' | 'charter' | 'verdana'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    darkMode: false,
    fontFamily: 'system' as FontFamily,
    fontSize: 16,
  }),
  actions: {
    toggleDarkMode() {
      this.darkMode = !this.darkMode
      this.apply()
    },
    setFontFamily(font: FontFamily) {
      this.fontFamily = font
      this.apply()
    },
    setFontSize(size: number) {
      this.fontSize = Math.min(Math.max(size, 12), 24)
      this.apply()
    },
    apply() {
      document.documentElement.setAttribute('data-theme', this.darkMode ? 'dark' : 'light')
      document.documentElement.setAttribute('data-font', this.fontFamily)
      document.documentElement.style.setProperty('--font-size-base', `${this.fontSize}px`)
    },
    init() {
      this.apply()
    },
  },
  persist: true,
})
