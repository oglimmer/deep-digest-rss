<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import NewsSection from './NewsSection.vue'
import { fetchFeeds, fetchNews, fetchTagGroup } from '@/services/remote'
import type { FeedEntry, NewsEntry } from '@/interfaces.ts'

const feedEntries = ref<FeedEntry[]>([])
const selectedFeeds = ref<number[]>([])
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

const previousDay = async () => {
  daysAgo.value += 1
  newsEntries.value = await fetchNews(daysAgoToDate(), []) // fetch without server-side feed filtering
  tagGroupData = await fetchTagGroup(daysAgoToDate())
  tagGroupKeys.value = Object.keys(tagGroupData)
}

const nextDay = async () => {
  if (daysAgo.value > 0) {
    daysAgo.value -= 1
    newsEntries.value = await fetchNews(daysAgoToDate(), []) // fetch without server-side feed filtering
    tagGroupData = await fetchTagGroup(daysAgoToDate())
    tagGroupKeys.value = Object.keys(tagGroupData)
  }
}

const refreshNews = async () => {
  loading.value = true
  newsEntries.value = await fetchNews(daysAgoToDate(), []) // fetch without server-side feed filtering
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
  fetchNews(daysAgoToDate(), selectedFeeds.value).then((data) => {
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
  const filteredNewsEntries = selectedFeeds.value.length > 0
    ? newsEntries.value.filter(entry => selectedFeeds.value.includes(entry.feedId))
    : newsEntries.value

  for (const key of tagGroupKeys.value) {
    const tags = tagGroupData[key] || []
    counts[key] = filteredNewsEntries.filter((entry) =>
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
    : 'none'
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
    : 'none'
})

// Global function to close both dropdowns
const closeAllDropdowns = () => {
  dropdownOpen.value = false
  dropdownExcludedOpen.value = false
}

// --- Filtering Logic ---
// 1. Remove entries flagged as ads (if excludeAds is set).
// 2. Filter news by selected feeds (client side) similar to tag selection.
// 3. Process tag group inclusion and exclusion.
const filteredNews = computed(() => {
  let filtered = newsEntries.value.filter(
    (entry) => !(excludeAds.value && entry.advertising)
  )
  
  // Client-side feed filtering
  if (selectedFeeds.value.length > 0) {
    filtered = filtered.filter(entry => selectedFeeds.value.includes(entry.feedId))
  }

  if (selectedTagGroups.value.length > 0) {
    const allowedTags = new Set<string>()
    selectedTagGroups.value.forEach(group => {
      ;(tagGroupData[group] || []).forEach(tag => allowedTags.add(tag))
    })
    filtered = filtered.filter(entry => entry.tags.some(tag => allowedTags.has(tag)))
  }

  if (excludedTagGroups.value.length > 0) {
    const excludedTags = new Set<string>()
    excludedTagGroups.value.forEach(group => {
      ;(tagGroupData[group] || []).forEach(tag => excludedTags.add(tag))
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

// Updated computed property to calculate counts from total newsEntries
const feedNewsCounts = computed(() => {
    const counts: Record<number, number> = {}
    for (const feed of feedEntries.value) {
        // Count entries for this feed from the complete set of news entries
        counts[feed.id] = newsEntries.value.filter(entry => entry.feedId === feed.id).length
    }
    return counts
})

const feedDropdownLabel = computed(() => {
  return selectedFeeds.value.length > 0
    ? selectedFeeds.value.map(id => feedEntries.value.find(feed => feed.id === id)?.title).join(', ')
    : 'Filter Feeds'
})

const dropdownFeedOpen = ref(false)
const toggleFeedDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownFeedOpen.value = !dropdownFeedOpen.value
}
</script>

<template>
  <div>
    <h2 @click="refreshNews">
      Lesbare Nachrichten für den {{ formattedOldestNewsDate }}
    </h2>
    <!-- Custom styled feed select with a typical arrow -->
    <div class="dropdown" @click.stop="toggleFeedDropdown">
      <div class="dropdown-header">
        <span>{{ feedDropdownLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownFeedOpen ? '▲' : '▼' }}</span>
      </div>
      <div class="dropdown-menu" v-if="dropdownFeedOpen" @click.stop>
        <label v-for="feed in feedEntries" :key="feed.id" class="dropdown-item">
          <input type="checkbox" :value="feed.id" v-model="selectedFeeds" />
          {{ feed.title }} ({{ feedNewsCounts[feed.id] || 0 }})
        </label>
      </div>
    </div>
    <!-- Dropdown for including tag groups -->
    <div class="dropdown" @click.stop="toggleDropdown">
      <div class="dropdown-header">
        <span>Must have: {{ dropdownLabel }}</span>
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
        <span>Must not: {{ dropdownExcludedLabel }}</span>
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
  <div class="control-wrapper">
      <button @click="previousDay" class="custom-button">Previous Day</button>
      <button @click="nextDay" :disabled="daysAgo === 0" class="custom-button">Next Day</button>
    </div>
    <!-- Loading Spinner -->
    <div v-if="loading" class="loading-spinner">
      <div class="spinner"></div>
    </div>
</template>

<style scoped>
/* Custom select styling similar to our dropdown look */
.custom-select {
  padding: 4px 8px;
  padding-right: 36px; /* Extra space for arrow */
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  font-size: 1rem;
  min-width: 200px;
  outline: none;
  /* Remove default arrow */
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  /* Add a background arrow icon */
  background-image: url("data:image/svg+xml,%3Csvg fill='gray' height='24' viewBox='0 0 24 24' width='24' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
  transition: background-color 0.2s, border-color 0.2s;
}
.custom-select:hover {
  background-color: #f9f9f9;
  border-color: #bbb;
}

/* Custom button styling */
.custom-button {
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  font-size: 1rem;
  margin-right: 8px;
  min-width: 100px;
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
}
.custom-button:hover:not(:disabled) {
  background-color: #f0f0f0;
  border-color: #bbb;
}
.custom-button:disabled {
  background-color: #f7f7f7;
  border-color: #ddd;
  color: #999;
  cursor: not-allowed;
}

/* Wrapper for controls to add consistent spacing */
.control-wrapper {
  display: inline-block;
  vertical-align: middle;
}

/* Dropdown styles */
.dropdown {
  display: inline-block;
  position: relative;
  margin: 0px 8px 4px 0px;
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  user-select: none;
  min-width: 200px;
  transition: border-color 0.2s;
}
.dropdown:hover {
  border-color: #bbb;
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
  transition: background-color 0.2s;
}
.dropdown-item:hover {
  background-color: #f0f0f0;
}

/* Loading spinner styles */
.loading-spinner {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left-color: #000;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
