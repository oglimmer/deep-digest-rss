import { defineStore } from 'pinia'
import { login as apiLogin } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    loggedIn: false,
    email: '',
    authToken: '',
  }),
  getters: {
    authHeader(state): string {
      return state.loggedIn
        ? `Basic ${btoa(state.email + ':' + state.authToken)}`
        : `Basic ${btoa(__API_USER__ + ':' + __API_PASSWORD__)}`
    },
  },
  actions: {
    async login(email: string, password: string): Promise<string> {
      const token = await apiLogin(email, password)
      if (token) {
        this.loggedIn = true
        this.email = email
        this.authToken = token
        return ''
      }
      return 'Login Failed'
    },
    logout() {
      this.loggedIn = false
      this.email = ''
      this.authToken = ''
    },
  },
  persist: true,
})
