
export interface NewsEntry {
  id: number
  feedId: number
  createdOn: string
  url: string
  text: string
  title: string
  advertising: boolean
  tags: string[]
}

export interface FeedEntry {
  id: number
  url: string
  title: string
  createdOn: string
}
