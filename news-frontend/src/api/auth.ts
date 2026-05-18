import { apiFetch, ApiError } from './client'

export interface AuthMeResponse {
  email: string
  roles: string[]
}

export const login = async (email: string, password: string): Promise<AuthMeResponse | null> => {
  try {
    return await apiFetch<AuthMeResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: { email, password },
      silentOn401: true,
    })
  } catch (error) {
    if (error instanceof ApiError && (error.status === 401 || error.status === 429)) {
      return null
    }
    console.error('login error:', error)
    return null
  }
}

export const logout = async (): Promise<void> => {
  try {
    await apiFetch<void>('/api/v1/auth/logout', { method: 'POST', silentOn401: true })
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) return
    console.error('logout error:', error)
  }
}

export const me = async (): Promise<AuthMeResponse | null> => {
  try {
    return await apiFetch<AuthMeResponse>('/api/v1/auth/me', { silentOn401: true })
  } catch (error) {
    // Both 401 (no session) and 403 (anonymous lacks role) mean "not logged in" here.
    if (error instanceof ApiError && (error.status === 401 || error.status === 403)) return null
    console.error('me error:', error)
    return null
  }
}
