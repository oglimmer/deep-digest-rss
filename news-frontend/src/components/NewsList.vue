<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useCookies } from '@vueuse/integrations/useCookies'
import NewsSection from './NewsSection.vue';
import { fetchFeeds, fetchNews, fetchTagGroup } from '@/servies/remote.ts'
import type { FeedEntry, NewsEntry } from '@/interfaces.ts'


const feedEntries = ref<FeedEntry[]>([])
const selectedFeed = ref(0)
const tagGroupKeys = ref<string[]>([])
const selectedTagGroup = ref('')

const newsEntries = ref<NewsEntry[]>([])
const daysAgo = ref(0)

const cookies = useCookies()
const excludeAds = ref(cookies.get('excludeAds') === true)
const loading = ref(false)

watch(excludeAds, (newValue) => {
  const expirationDate = new Date()
  expirationDate.setDate(expirationDate.getDate() + 365)
  cookies.set('excludeAds', newValue, { expires: expirationDate })
})

watch(selectedFeed, async () => {
  newsEntries.value = await fetchNews(daysAgoToDate(), selectedFeed.value)
})

const previousDay = async () => {
  daysAgo.value += 1
  newsEntries.value = await fetchNews(daysAgoToDate(), selectedFeed.value)
  tagGroupData = await fetchTagGroup(daysAgoToDate())
  tagGroupKeys.value = Object.keys(tagGroupData)
}

const nextDay = async () => {
  if (daysAgo.value > 0) {
    daysAgo.value -= 1
    newsEntries.value = await fetchNews(daysAgoToDate(), selectedFeed.value)
    tagGroupData = await fetchTagGroup(daysAgoToDate())
    tagGroupKeys.value = Object.keys(tagGroupData)
  }
}

onMounted(async () => {
  fetchTagGroup(daysAgoToDate()).then((data) => {
    tagGroupData = data
    tagGroupKeys.value = Object.keys(tagGroupData)
  })
  fetchFeeds().then((data) => {
    feedEntries.value = data
  })
  fetchNews(daysAgoToDate(), selectedFeed.value).then((news) => {
    newsEntries.value = news
  })
})

const refreshNews = async () => {
  loading.value = true
  await fetchNews(daysAgoToDate(), selectedFeed.value)
  loading.value = false
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
  return daysAgoToDate().toLocaleDateString()
});

const daysAgoToDate = () : Date => {
  const date = new Date()
  date.setDate(date.getDate() - daysAgo.value)
  return date
};
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
    <button @click="refreshNews" :disabled="loading" :class="{ 'loading': loading }">Refresh</button> |
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

      <NewsSection :newsEntries="nightNews" sectionHeader="Night News" :feedEntries="feedEntries" />
      <NewsSection :newsEntries="afternoonNews" sectionHeader="Afternoon News" :feedEntries="feedEntries" />
      <NewsSection :newsEntries="morningNews" sectionHeader="Morning News" :feedEntries="feedEntries" />

    </div>
    <p v-else>Keine Nachrichten für diesen Tag</p>
  </div>
</template>

<style scoped>
.loading {
  background-color: grey;
  cursor: not-allowed;
}
</style>
