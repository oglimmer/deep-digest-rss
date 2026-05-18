export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly statusText: string,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export const AUTH_EXPIRED_EVENT = 'auth:expired'

type QueryValue = string | number | boolean | undefined | null
type Query = Record<string, QueryValue | QueryValue[]>

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE'
  query?: Query
  body?: unknown
  // If true, a 401 response will NOT dispatch an auth:expired event. Useful for the
  // initial /auth/me probe where 401 just means "not logged in".
  silentOn401?: boolean
}

const buildQuery = (query?: Query): string => {
  if (!query) return ''
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null) continue
    if (Array.isArray(value)) {
      const joined = value.filter((v) => v !== undefined && v !== null).join(',')
      if (joined.length > 0) params.set(key, joined)
    } else {
      params.set(key, String(value))
    }
  }
  const s = params.toString()
  return s ? `?${s}` : ''
}

const dateToYMD = (date: Date): string => {
  const y = date.getFullYear()
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  return `${y}-${m}-${d}`
}

const readCookie = (name: string): string | null => {
  if (typeof document === 'undefined') return null
  const prefix = name + '='
  for (const part of document.cookie.split(';')) {
    const trimmed = part.trim()
    if (trimmed.startsWith(prefix)) {
      return decodeURIComponent(trimmed.substring(prefix.length))
    }
  }
  return null
}

const MUTATING_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE'])

export const apiFetch = async <T = unknown>(
  path: string,
  options: RequestOptions = {},
): Promise<T> => {
  const { method = 'GET', query, body, silentOn401 = false } = options

  const headers: Record<string, string> = {}
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  if (MUTATING_METHODS.has(method)) {
    const csrf = readCookie('XSRF-TOKEN')
    if (csrf) headers['X-XSRF-TOKEN'] = csrf
  }

  const response = await fetch(`${__API_URL__}${path}${buildQuery(query)}`, {
    method,
    headers,
    credentials: 'include',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (response.status === 401 && !silentOn401) {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT))
    }
  }

  if (!response.ok) {
    throw new ApiError(
      response.status,
      response.statusText,
      `${method} ${path} failed: ${response.status}`,
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) {
    return (await response.json()) as T
  }
  return undefined as T
}

export { dateToYMD }
