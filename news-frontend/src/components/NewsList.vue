<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'

interface NewsEntry {
  refId: string
  url: string
  text: string
  title: string
  id: number
  createdOn: string
}

const newsEntries = ref<NewsEntry[]>([])
const daysAgo = ref(0)

const fetchNews = async (daysAgo: number) => {
  try {
    const response = await fetch(`${__API_URL__}/api/v1/news?daysAgo=${daysAgo}`)
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
  fetchNews(daysAgo.value)
}

const nextDay = () => {
  if (daysAgo.value > 0) {
    daysAgo.value -= 1
    fetchNews(daysAgo.value)
  }
}

onMounted(() => {
  fetchNews(daysAgo.value)
})

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
  if (newsEntries.value.length === 0) {
    return ''
  }
  return new Date(newsEntries.value?.[0].createdOn).toLocaleDateString()
});
</script>

<template>
  <div>
    <h2>Lesbare Nachrichten für den {{ formattedOldestNewsDate }}</h2>
    <button @click="previousDay">Previous Day</button>
    <button @click="nextDay" :disabled="daysAgo === 0">Next Day</button>
    <div v-if="newsEntries.length > 0">

      <h3 v-if="nightNews.length > 0">Night News</h3>
      <ul v-if="nightNews.length > 0">
        <li v-for="entry in nightNews" :key="entry.id">
          <a :href="entry.url" target="_blank">[1]</a> {{ entry.title }}
          <p>{{ entry.text }}</p>
        </li>
      </ul>

      <h3 v-if="afternoonNews.length > 0">Afternoon News</h3>
      <ul v-if="afternoonNews.length > 0">
        <li v-for="entry in afternoonNews" :key="entry.id">
          <a :href="entry.url" target="_blank">[1]</a> {{ entry.title }}
          <p>{{ entry.text }}</p>
        </li>
      </ul>

      <h3 v-if="morningNews.length > 0">Morning News</h3>
      <ul v-if="morningNews.length > 0">
        <li v-for="entry in morningNews" :key="entry.id">
          <a :href="entry.url" target="_blank">[1]</a> {{ entry.title }}
          <p>{{ entry.text }}</p>
        </li>
      </ul>

    </div>
    <p v-else>Keine Nachrichten für diesen Tag</p>
  </div>
</template>

<style scoped>

</style>
