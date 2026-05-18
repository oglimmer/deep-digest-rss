import type { DailyDigestEntry } from '@/interfaces'
import { apiFetch, ApiError, dateToYMD } from './client'

export const fetchDailyDigest = async (date: Date): Promise<DailyDigestEntry | null> => {
  try {
    return await apiFetch<DailyDigestEntry>('/api/v1/daily-digest', {
      query: { date: dateToYMD(date) },
    })
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return null
    }
    throw error
  }
}
