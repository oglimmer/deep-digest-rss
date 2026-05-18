import { useAuthStore } from '@/stores/auth'

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

type QueryValue = string | number | boolean | undefined | null
type Query = Record<string, QueryValue | QueryValue[]>

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE'
  query?: Query
  body?: unknown
  auth?: boolean
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

export const apiFetch = async <T = unknown>(
  path: string,
  options: RequestOptions = {},
): Promise<T> => {
  const { method = 'GET', query, body, auth = true } = options

  const headers: Record<string, string> = {}
  if (auth) {
    headers.Authorization = useAuthStore().authHeader
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(`${__API_URL__}${path}${buildQuery(query)}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    throw new ApiError(response.status, response.statusText, `${method} ${path} failed: ${response.status}`)
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
