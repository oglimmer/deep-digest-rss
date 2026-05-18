import type { NewsEntry } from '@/interfaces'
import { apiFetch, dateToYMD } from './client'

export const fetchNews = (date: Date, feedIdList: number[] = []): Promise<NewsEntry[]> =>
  apiFetch<NewsEntry[]>('/api/v1/news', {
    query: { date: dateToYMD(date), feedIdList },
  })

export const fetchNewsById = (id: number): Promise<NewsEntry> =>
  apiFetch<NewsEntry>(`/api/v1/news/${id}`)

export const vote = (newsId: number, up: boolean): Promise<void> =>
  apiFetch<void>(`/api/v1/news/${newsId}/vote`, {
    method: 'POST',
    body: { vote: up },
  })
