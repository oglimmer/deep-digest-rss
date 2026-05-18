import { apiFetch, ApiError } from './client'

interface AuthResponse {
  authToken: string
}

export const login = async (email: string, password: string): Promise<string | null> => {
  try {
    const response = await apiFetch<AuthResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: { email, password },
      auth: false,
    })
    return response.authToken
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      return null
    }
    console.error('login error:', error)
    return null
  }
}
