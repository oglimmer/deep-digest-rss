

const Authorization = `Basic ${btoa(__API_USER__+':'+__API_USER__)}`;

const dateToYearMonthDay = (date: Date) : string => {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

export const fetchFeeds = async () => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/feed`, {
      headers: { Authorization }
    })
    if (response.ok) {
      return await response.json()
    } else {
      console.error('Failed to fetch news entries')
    }
  } catch (error) {
    console.error('Error fetching news entries:', error)
  }
}

export const fetchNews = async (date: Date, feedId: number) => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/news?date=${dateToYearMonthDay(date)}&feedId=${feedId}`, {
      headers: { Authorization }
    })
    if (response.ok) {
      return await response.json()
    } else {
      console.error('Failed to fetch news entries')
    }
  } catch (error) {
    console.error('Error fetching news entries:', error)
  }
}

export const fetchTagGroup = async (date: Date) => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/tag-group?date=${dateToYearMonthDay(date)}`, {
      headers: { Authorization }
    })
    if (response.ok) {
      return await response.json()
    } else {
      console.error('Failed to fetch tag group data')
    }
  } catch (error) {
    console.error('Error fetching tag group data:', error)
  }
};
