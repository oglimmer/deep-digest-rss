<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import NewsSection from './NewsSection.vue'
import { fetchFeeds, fetchNews, fetchTagGroup } from '@/servies/remote.ts'
import type { FeedEntry, NewsEntry } from '@/interfaces.ts'

const feedEntries = ref<FeedEntry[]>([])
const selectedFeed = ref(0)
const tagGroupKeys = ref<string[]>([])

// For including tag groups (only show news matching these, if any are selected)
const selectedTagGroups = ref<string[]>([])
// For excluding tag groups (hide news matching any of these)
const excludedTagGroups = ref<string[]>([])

const newsEntries = ref<NewsEntry[]>([])
const daysAgo = ref(0)
const excludeAds = ref(false)
const loading = ref(false)

let tagGroupData: Record<string, string[]> = {}

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

const refreshNews = async () => {
  loading.value = true
  newsEntries.value = await fetchNews(daysAgoToDate(), selectedFeed.value)
  tagGroupData = await fetchTagGroup(daysAgoToDate())
  loading.value = false
}

onMounted(async () => {
  // Fetch initial tag groups, feeds, and news
  fetchTagGroup(daysAgoToDate()).then((data) => {
    tagGroupData = data
    tagGroupKeys.value = Object.keys(tagGroupData)
  })
  fetchFeeds().then((data) => {
    feedEntries.value = data
  })
  fetchNews(daysAgoToDate(), selectedFeed.value).then((data) => {
    newsEntries.value = data
  })

  // Add a global click listener to close both dropdowns when clicking outside
  document.addEventListener('click', closeAllDropdowns)
})

onUnmounted(() => {
  document.removeEventListener('click', closeAllDropdowns)
})

// --- Computed property for tag group counts ---
const tagGroupCounts = computed(() => {
  const counts: Record<string, number> = {}
  for (const key of tagGroupKeys.value) {
    const tags = tagGroupData[key] || []
    counts[key] = newsEntries.value.filter((entry) =>
      entry.tags.some((tag) => tags.includes(tag))
    ).length
  }
  return counts
})

// --- Dropdown for Including Tag Groups ---
const dropdownOpen = ref(false)
const toggleDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownOpen.value = !dropdownOpen.value
}

const dropdownLabel = computed(() => {
  return selectedTagGroups.value.length > 0
    ? selectedTagGroups.value.join(', ')
    : 'Select Tag Groups'
})

// --- Dropdown for Excluding Tag Groups ---
const dropdownExcludedOpen = ref(false)
const toggleExcludedDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownExcludedOpen.value = !dropdownExcludedOpen.value
}

const dropdownExcludedLabel = computed(() => {
  return excludedTagGroups.value.length > 0
    ? excludedTagGroups.value.join(', ')
    : 'Exclude Tag Groups'
})

// Global function to close both dropdowns
const closeAllDropdowns = () => {
  dropdownOpen.value = false
  dropdownExcludedOpen.value = false
}

// --- Filtering Logic ---
// First, filter out entries flagged as ads (if needed).
// Then, if any tag groups are selected to include, restrict to news entries that have at least one matching tag.
// Finally, if any tag groups are selected for exclusion, remove any news entry that has any tag belonging to the excluded groups.
const filteredNews = computed(() => {
  let filtered = newsEntries.value.filter(
    (entry) => !(excludeAds.value && entry.advertising)
  )

  if (selectedTagGroups.value.length > 0) {
    const allowedTags = new Set<string>()
    selectedTagGroups.value.forEach(group => {
      (tagGroupData[group] || []).forEach(tag => allowedTags.add(tag))
    })
    filtered = filtered.filter(entry => entry.tags.some(tag => allowedTags.has(tag)))
  }

  if (excludedTagGroups.value.length > 0) {
    const excludedTags = new Set<string>()
    excludedTagGroups.value.forEach(group => {
      (tagGroupData[group] || []).forEach(tag => excludedTags.add(tag))
    })
    filtered = filtered.filter(entry => !entry.tags.some(tag => excludedTags.has(tag)))
  }

  return filtered
})

const morningNews = computed(() => {
  return filteredNews.value.filter((entry) => {
    const hour = new Date(entry.createdOn).getHours()
    return hour >= 0 && hour < 12
  })
})

const afternoonNews = computed(() => {
  return filteredNews.value.filter((entry) => {
    const hour = new Date(entry.createdOn).getHours()
    return hour >= 12 && hour < 18
  })
})

const nightNews = computed(() => {
  return filteredNews.value.filter((entry) => {
    const hour = new Date(entry.createdOn).getHours()
    return hour >= 18 && hour < 24
  })
})

const formattedOldestNewsDate = computed(() => {
  return daysAgoToDate().toLocaleDateString()
})

const daysAgoToDate = (): Date => {
  const date = new Date()
  date.setDate(date.getDate() - daysAgo.value)
  return date
}
</script>

<template>
  <div>
    <h2>Lesbare Nachrichten für den {{ formattedOldestNewsDate }}</h2>
    <select v-model="selectedFeed">
      <option value="0">Alle Feeds</option>
      <option v-for="feed in feedEntries" :key="feed.id" :value="feed.id">
        {{ feed.title }}
      </option>
    </select>
    &nbsp;
    <button @click="previousDay">Previous Day</button>
    &nbsp;
    <button @click="nextDay" :disabled="daysAgo === 0">Next Day</button>
    &nbsp;
    <button @click="refreshNews" :disabled="loading" :class="{ loading: loading }">
      Refresh
    </button>
    <!-- Dropdown for including tag groups -->
    <div class="dropdown" @click.stop="toggleDropdown">
      <div class="dropdown-header">
        <span>Only: {{ dropdownLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownOpen ? '▲' : '▼' }}</span>
      </div>
      <div class="dropdown-menu" v-if="dropdownOpen" @click.stop>
        <label v-for="key in tagGroupKeys" :key="key" class="dropdown-item">
          <input type="checkbox" :value="key" v-model="selectedTagGroups" />
          {{ key }} ({{ tagGroupCounts[key] }})
        </label>
      </div>
    </div>
    <!-- Dropdown for excluding tag groups -->
    <div class="dropdown" @click.stop="toggleExcludedDropdown">
      <div class="dropdown-header">
        <span>Exclude: {{ dropdownExcludedLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownExcludedOpen ? '▲' : '▼' }}</span>
      </div>
      <div class="dropdown-menu" v-if="dropdownExcludedOpen" @click.stop>
        <label v-for="key in tagGroupKeys" :key="key" class="dropdown-item">
          <input type="checkbox" :value="key" v-model="excludedTagGroups" />
          {{ key }} ({{ tagGroupCounts[key] }})
        </label>
      </div>
    </div>
    <div v-if="filteredNews.length > 0">
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

/* Dropdown styles */
.dropdown {
  display: inline-block;
  position: relative;
  margin-left: 10px;
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  user-select: none;
  min-width: 200px;
}

.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dropdown-arrow {
  margin-left: 8px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 10;
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-top: 4px;
  padding: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  max-height: 200px;
  overflow-y: auto;
  min-width: 200px;
}

.dropdown-item {
  display: block;
  padding: 4px;
  cursor: pointer;
}

.dropdown-item:hover {
  background-color: #f0f0f0;
}
</style>
