<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'

const Authorization = `Basic ${btoa(__API_USER__+':'+__API_USER__)}`;

interface NewsEntry {
  id: number
  feedId: number
  createdOn: string
  url: string
  text: string
  title: string
}

interface FeedEntry {
  id: number
  url: string
  title: string
  createdOn: string
}

const feedEntries = ref<FeedEntry[]>([])
const selectedFeed = ref(0)

const newsEntries = ref<NewsEntry[]>([])
const daysAgo = ref(0)

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
})

const refreshNews = () => {
  fetchNews(daysAgo.value, selectedFeed.value)
}

const morningNews = computed(() => {
  return newsEntries.value.filter((entry) => {
    const hour = new Date(entry.createdOn).getHours()
    return hour >= 0 && hour < 12
  })
})

const afternoonNews = computed(() => {
  return newsEntries.value.filter((entry) => {
    const hour = new Date(entry.createdOn).getHours()
    return hour >= 12 && hour < 18
  })
})

const nightNews = computed(() => {
  return newsEntries.value.filter((entry) => {
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
    <button @click="previousDay" :disabled="newsEntries.length === 0">Previous Day</button> &nbsp;
    <button @click="nextDay" :disabled="daysAgo === 0">Next Day</button> &nbsp;
    <button @click="refreshNews">Refresh</button>
    <div v-if="newsEntries.length > 0">

      <h3 v-if="nightNews.length > 0">Night News</h3>
      <ul v-if="nightNews.length > 0">
        <li v-for="entry in nightNews" :key="entry.id">
          <a :href="entry.url" target="_blank">[{{ entry.feedId }}]</a> {{ entry.title }}
          <p>{{ entry.text }}</p>
        </li>
      </ul>

      <h3 v-if="afternoonNews.length > 0">Afternoon News</h3>
      <ul v-if="afternoonNews.length > 0">
        <li v-for="entry in afternoonNews" :key="entry.id">
          <a :href="entry.url" target="_blank">[{{ entry.feedId }}]</a> {{ entry.title }}
          <p>{{ entry.text }}</p>
        </li>
      </ul>

      <h3 v-if="morningNews.length > 0">Morning News</h3>
      <ul v-if="morningNews.length > 0">
        <li v-for="entry in morningNews" :key="entry.id">
          <a :href="entry.url" target="_blank">[{{ entry.feedId }}]</a> {{ entry.title }}
          <p>{{ entry.text }}</p>
        </li>
      </ul>

    </div>
    <p v-else>Keine Nachrichten für diesen Tag</p>
  </div>
</template>

<style scoped>

</style>
