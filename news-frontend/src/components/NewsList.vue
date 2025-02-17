<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onClickOutside } from '@vueuse/core'
import NewsSection from './NewsSection.vue'
import LoginForm from './LoginForm.vue'
import { useDataStore } from '@/stores/data'
import { daysAgoToDate } from "@/services/temporal"

const dataStore = useDataStore()

// const excludeAds = ref(dataStore.excludeAds)
const loading = ref(false)

const previousDay = async () => {
  dataStore.daysAgo += 1
  refreshNews()
}

const nextDay = async () => {
  if (dataStore.daysAgo > 0) {
    dataStore.daysAgo -= 1
    refreshNews()
  }
}

const refreshNews = async () => {
  loading.value = true
  await dataStore.fetchNews()
  await dataStore.fetchTagGroup()
  loading.value = false
}

onMounted(async () => {
  // Fetch initial tag groups, feeds, and news
  dataStore.fetchTagGroup()
  dataStore.fetchFeeds()
  dataStore.fetchNews()
})

// --- Dropdown for Including Tag Groups ---
const dropdownOpen = ref(false)
const toggleDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownOpen.value = !dropdownOpen.value
}

const dropdownLabel = computed(() => {
  return dataStore.selectedTagGroups.length > 0
    ? dataStore.selectedTagGroups.join(', ')
    : 'none'
})

// --- Dropdown for Excluding Tag Groups ---
const dropdownExcludedOpen = ref(false)
const toggleExcludedDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownExcludedOpen.value = !dropdownExcludedOpen.value
}

const dropdownExcludedLabel = computed(() => {
  return dataStore.excludedTagGroups.length > 0
    ? dataStore.excludedTagGroups.join(', ')
    : 'none'
})

// Global function to close all dropdowns
const closeAllDropdowns = () => {
  dropdownOpen.value = false
  dropdownExcludedOpen.value = false
  dropdownFeedOpen.value = false
}

const formattedOldestNewsDate = computed(() => {
  return daysAgoToDate(dataStore.daysAgo).toLocaleDateString()
})

const feedNewsCounts = computed(() => {
  const counts: Record<number, number> = {}
  for (const feed of dataStore.feedEntries) {
    counts[feed.id] = dataStore.newsEntries.filter(entry => entry.feedId === feed.id).length
  }
  return counts
})

const feedDropdownLabel = computed(() => {
  return dataStore.selectedFeeds.length > 0
    ? dataStore.selectedFeeds.map(id =>
        dataStore.feedEntries.find(feed => feed.id === id)?.title
      ).join(', ')
    : 'Filter Feeds'
})

const dropdownFeedOpen = ref(false)
const toggleFeedDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownFeedOpen.value = !dropdownFeedOpen.value
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// --- Modal for Login ---
const showModal = ref(false)
const toggleModal = () => {
  showModal.value = !showModal.value
}

const refContainerFeed = ref<HTMLElement|null>(null)
onClickOutside(refContainerFeed, closeAllDropdowns)
const refContainerIncludeTags = ref<HTMLElement|null>(null)
onClickOutside(refContainerIncludeTags, closeAllDropdowns)
const refContainerExcludeTags = ref<HTMLElement|null>(null)
onClickOutside(refContainerExcludeTags, closeAllDropdowns)
</script>

<template>
  <div>
    <h2 @click="refreshNews">
      Lesbare Nachrichten für den {{ formattedOldestNewsDate }}
    </h2>
    <!-- Login Button -->
    <button v-if="!dataStore.loggedIn" @click="toggleModal" class="login-button">L</button>
    <!-- Custom styled feed select with a typical arrow -->
    <div class="dropdown" @click.stop="toggleFeedDropdown" ref="refContainerFeed">
      <div class="dropdown-header">
        <span>{{ feedDropdownLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownFeedOpen ? '▲' : '▼' }}</span>
      </div>
      <div class="dropdown-menu" v-if="dropdownFeedOpen" @click.stop>
        <label v-for="feed in dataStore.feedEntries" :key="feed.id" class="dropdown-item">
          <input type="checkbox" :value="feed.id" v-model="dataStore.selectedFeeds" />
          {{ feed.title }} ({{ feedNewsCounts[feed.id] || 0 }})
        </label>
      </div>
    </div>
    <!-- Dropdown for including tag groups -->
    <div class="dropdown" @click.stop="toggleDropdown" ref="refContainerIncludeTags">
      <div class="dropdown-header">
        <span>Must have: {{ dropdownLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownOpen ? '▲' : '▼' }}</span>
      </div>
      <div class="dropdown-menu" v-if="dropdownOpen" @click.stop>
        <label v-for="key in dataStore.tagGroupKeys" :key="key" class="dropdown-item">
          <input type="checkbox" :value="key" v-model="dataStore.selectedTagGroups" />
          {{ key }} ({{ dataStore.tagGroupCounts[key] }})
        </label>
      </div>
    </div>
    <!-- Dropdown for excluding tag groups -->
    <div class="dropdown" @click.stop="toggleExcludedDropdown" ref="refContainerExcludeTags">
      <div class="dropdown-header">
        <span>Must not: {{ dropdownExcludedLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownExcludedOpen ? '▲' : '▼' }}</span>
      </div>
      <div class="dropdown-menu" v-if="dropdownExcludedOpen" @click.stop>
        <label v-for="key in dataStore.tagGroupKeys" :key="key" class="dropdown-item">
          <input type="checkbox" :value="key" v-model="dataStore.excludedTagGroups" />
          {{ key }} ({{ dataStore.tagGroupCounts[key] }})
        </label>
      </div>
    </div>
  </div>
  <div v-if="dataStore.filteredNews.length > 0">
    <NewsSection :newsEntries="dataStore.nightNews" sectionHeader="Night News" :feedEntries="dataStore.feedEntries" />
    <NewsSection :newsEntries="dataStore.afternoonNews" sectionHeader="Afternoon News" :feedEntries="dataStore.feedEntries" />
    <NewsSection :newsEntries="dataStore.morningNews" sectionHeader="Morning News" :feedEntries="dataStore.feedEntries" />
  </div>
  <p v-else>Keine Nachrichten für diesen Tag</p>

  <div class="control-wrapper">
    <button @click="previousDay" :disabled="loading" class="custom-button">Previous Day</button>
    <button @click="nextDay" :disabled="loading || dataStore.daysAgo === 0" class="custom-button">Next Day</button>
    <button @click="scrollToTop" class="custom-button">Scroll to top</button>
  </div>

  <!-- Loading Spinner -->
  <div v-if="loading" class="loading-spinner">
    <div class="spinner"></div>
  </div>
  <!-- Modal Overlay -->
  <div v-if="showModal" class="modal-overlay" @click="toggleModal">
    <div class="modal-content" @click.stop>
      <LoginForm :closeModal="toggleModal" />
    </div>
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

h2 {
  cursor: pointer;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: relative;
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s, border-color 0.2s;
}
.close-button:hover {
  background-color: #f0f0f0;
  border-color: #bbb;
}

.login-button {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s, border-color 0.2s;
}
.login-button:hover {
  background-color: #f0f0f0;
  border-color: #bbb;
}
</style>
