import { useDataStore } from '@/stores/data';


const dataStore: {
  _dataStore: ReturnType<typeof useDataStore> | null;
  get: () => ReturnType<typeof useDataStore>;
} = {
  _dataStore: null,
  get() {
    return this._dataStore ??= useDataStore();
  }
}

const dateToYearMonthDay = (date: Date) : string => {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

export const fetchFeeds = async () => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/feed`, {
      headers: { Authorization: dataStore.get().authentizationHeader }
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

export const fetchNews = async (date: Date, feedIdList: number[]) => {
  try {
    const url = `${__API_URL__}/api/v1/news?date=${dateToYearMonthDay(date)}&feedIdList=${feedIdList}`
    const response = await fetch(url, {
      headers: { Authorization: dataStore.get().authentizationHeader }
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
      headers: { Authorization: dataStore.get().authentizationHeader }
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

export const login = async (email: string, password: string) => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: email,
        password: password
      })
    });
    if (response.ok) {
      const responseJson = await response.json();
      const authToken = responseJson.authToken;
      return authToken;
    }
  } catch (error) {
    console.error('Error:', error);
  }
};


export const vote = async (newsId: number, up: boolean) => {
  try {
    await fetch(`${__API_URL__}/api/v1/news/${newsId}/vote`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: dataStore.get().authentizationHeader
      },
      body: JSON.stringify({
        vote: up
      })
    });
  } catch (error) {
    console.error('Error:', error);
  }
};
