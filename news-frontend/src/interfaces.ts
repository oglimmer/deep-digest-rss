export type ImpactScope =
  | 'global'
  | 'international'
  | 'europa'
  | 'deutschland'
  | 'regional'
  | 'branche'

export interface NewsEntry {
  id: number
  feedId: number
  createdOn: string
  url: string
  text: string
  title: string
  advertising: boolean
  tags: string[]
  timely: boolean
  impactScope: ImpactScope
  voted: boolean
}

export interface FeedEntry {
  id: number
  url: string
  title: string
  cookie: string | null
  createdOn: string
}

export interface DailyDigestEntry {
  content: string
  createdOn: string
}
