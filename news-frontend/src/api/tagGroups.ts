import { apiFetch, dateToYMD } from './client'

export type TagGroupData = Record<string, string[]>

export const fetchTagGroup = (date: Date): Promise<TagGroupData> =>
  apiFetch<TagGroupData>('/api/v1/tag-group', {
    query: { date: dateToYMD(date) },
  })
