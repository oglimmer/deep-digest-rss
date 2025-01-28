<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useCookies } from '@vueuse/integrations/useCookies'
import NewsSection from './NewsSection.vue';

const Authorization = `Basic ${btoa(__API_USER__+':'+__API_USER__)}`;

interface NewsEntry {
  id: number
  feedId: number
  createdOn: string
  url: string
  text: string
  title: string
  advertising: boolean
  tags: string[]
}

interface FeedEntry {
  id: number
  url: string
  title: string
  createdOn: string
}

const feedEntries = ref<FeedEntry[]>([])
const selectedFeed = ref(0)
const tagGroupKeys = ref<string[]>([])
const selectedTagGroup = ref('')

const newsEntries = ref<NewsEntry[]>([])
const daysAgo = ref(0)

const cookies = useCookies()
const excludeAds = ref(cookies.get('excludeAds') === true)

watch(excludeAds, (newValue) => {
  const expirationDate = new Date()
  expirationDate.setDate(expirationDate.getDate() + 365)
  cookies.set('excludeAds', newValue, { expires: expirationDate })
})

const fetchFeeds = async () => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/feed`, {
      headers: { Authorization }
    })
    if (response.ok) {
      feedEntries.value = await response.json()
    } else {
      console.error('Failed to fetch news entries')
    }
  } catch (error) {
    console.error('Error fetching news entries:', error)
  }
}

watch(selectedFeed, () => {
  fetchNews(daysAgo.value, selectedFeed.value)
})

const fetchNews = async (daysAgo: number, feedId: number) => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/news?daysAgo=${daysAgo}&feedId=${feedId}`, {
      headers: { Authorization }
    })
    if (response.ok) {
      newsEntries.value = await response.json()
    } else {
      console.error('Failed to fetch news entries')
    }
  } catch (error) {
    console.error('Error fetching news entries:', error)
  }
}

const previousDay = () => {
  daysAgo.value += 1
  fetchNews(daysAgo.value, selectedFeed.value)
}

const nextDay = () => {
  if (daysAgo.value > 0) {
    daysAgo.value -= 1
    fetchNews(daysAgo.value, selectedFeed.value)
  }
}

onMounted(() => {
  fetchNews(daysAgo.value, selectedFeed.value)
  fetchFeeds()
  fetchTagGroup()
})

const refreshNews = () => {
  fetchNews(daysAgo.value, selectedFeed.value)
}

let tagGroupData: Record<string, string[]> = {}

const tagGroupCounts = computed(() => {
  const counts: Record<string, number> = {};
  for (const key of tagGroupKeys.value) {
    const tags = tagGroupData[key] || [];
    counts[key] = newsEntries.value.filter((entry) =>
      entry.tags.some((tag) => tags.includes(tag))
    ).length;
  }
  return counts;
});
  
const filteredNewsByTagGroup = computed(() => {
  if (!selectedTagGroup.value) {
    return newsEntries.value.filter((entry) => !(excludeAds.value && entry.advertising))
  }
  const tags = tagGroupData[selectedTagGroup.value] || [];
  return newsEntries.value.filter((entry) => !(excludeAds.value && entry.advertising) && entry.tags.some((tag) => tags.includes(tag)))
})

const fetchTagGroup = async () => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/tag-group`, {
      headers: { Authorization }
    })
    if (response.ok) {
      tagGroupData = await response.json()
      tagGroupKeys.value = Object.keys(tagGroupData)
    } else {
      console.error('Failed to fetch tag group data')
    }
  } catch (error) {
    console.error('Error fetching tag group data:', error)
  }
};

const morningNews = computed(() => {
  return filteredNewsByTagGroup.value.filter((entry) => {
    const hour = new Date(entry.createdOn).getHours()
    return hour >= 0 && hour < 12
  })
})

const afternoonNews = computed(() => {
  return filteredNewsByTagGroup.value.filter((entry) => {
    const hour = new Date(entry.createdOn).getHours()
    return hour >= 12 && hour < 18
  })
})

const nightNews = computed(() => {
  return filteredNewsByTagGroup.value.filter((entry) => {
    const hour = new Date(entry.createdOn).getHours()
    return hour >= 18 && hour < 24
  })
})

const formattedOldestNewsDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() - daysAgo.value)
  return date.toLocaleDateString()
});
</script>

<template>
  <div>
    <h2>Lesbare Nachrichten für den {{ formattedOldestNewsDate }}</h2>
    <select v-model="selectedFeed">
      <option value="0">Alle Feeds</option>
      <option v-for="feed in feedEntries" :key="feed.id" :value="feed.id">{{ feed.title }}</option>
    </select> &nbsp;
    <button @click="previousDay" :disabled="filteredNewsByTagGroup.length === 0">Previous Day</button> &nbsp;
    <button @click="nextDay" :disabled="daysAgo === 0">Next Day</button> &nbsp;
    <button @click="refreshNews">Refresh</button> |
    <label>
      Exclude Ads
      <input type="checkbox" v-model="excludeAds" />        
    </label> |
    Filter
    <select v-model="selectedTagGroup">
      <option value=""></option>
      <option v-for="key in tagGroupKeys" :key="key" :value="key">
        {{ key }} ({{ tagGroupCounts[key] }})
      </option>
    </select> &nbsp;
    <div v-if="filteredNewsByTagGroup.length > 0">

      <NewsSection :newsEntries="nightNews" sectionHeader="Night News" />
      <NewsSection :newsEntries="afternoonNews" sectionHeader="Afternoon News" />
      <NewsSection :newsEntries="morningNews" sectionHeader="Morning News" />

    </div>
    <p v-else>Keine Nachrichten für diesen Tag</p>
  </div>
</template>

<style scoped>

</style>
