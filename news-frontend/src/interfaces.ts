
export interface NewsEntry {
  id: number
  feedId: number
  createdOn: string
  url: string
  text: string
  title: string
  advertising: boolean
  tags: string[]
  voted: boolean
}

export interface FeedEntry {
  id: number
  url: string
  title: string
  createdOn: string
}
