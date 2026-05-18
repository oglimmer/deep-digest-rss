import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, me as apiMe } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    loggedIn: false,
    email: '',
    roles: [] as string[],
    hydrated: false,
  }),
  actions: {
    async hydrate() {
      const result = await apiMe()
      if (result) {
        this.loggedIn = true
        this.email = result.email
        this.roles = result.roles
      } else {
        this.loggedIn = false
        this.email = ''
        this.roles = []
      }
      this.hydrated = true
    },
    async login(email: string, password: string): Promise<string> {
      const result = await apiLogin(email, password)
      if (!result) return 'Login Failed'
      this.loggedIn = true
      this.email = result.email
      this.roles = result.roles
      this.hydrated = true
      return ''
    },
    async logout() {
      await apiLogout()
      this.clear()
    },
    clear() {
      this.loggedIn = false
      this.email = ''
      this.roles = []
    },
  },
})
