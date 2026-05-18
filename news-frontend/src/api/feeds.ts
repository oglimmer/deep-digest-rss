import type { FeedEntry } from '@/interfaces'
import { apiFetch } from './client'

export const fetchFeeds = (): Promise<FeedEntry[]> =>
  apiFetch<FeedEntry[]>('/api/v1/feed')
