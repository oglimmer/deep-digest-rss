<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onClickOutside } from '@vueuse/core'
import NewsSection from './NewsSection.vue'
import LoginForm from './LoginForm.vue'
import SingleNewsView from './SingleNewsView.vue'
import { useDataStore } from '@/stores/data'

const store = useDataStore()

// const excludeAds = ref(dataStore.excludeAds)
const loading = ref(false)

const changeDate = async (days: number) => {
  loading.value = true
  await store.changeDate(days)
  loading.value = false
}

const handleDeepLink = async (clearFilters: boolean) => {
  const match = window.location.hash.match(/^#article\/(\d+)$/)
  if (match) {
    const id = parseInt(match[1]!, 10)
    store.deepLinkedNewsId = id
    if (clearFilters) {
      store.selectedFeeds = []
      store.selectedTagGroups = []
      store.excludedTagGroups = []
      store.excludeAds = false
    }
    await store.fetchSingleNews(id)
    store.singleNewsMode = true
  }
}

const onHashChange = () => {
  handleDeepLink(false)
}

onMounted(async () => {
  // Fetch initial tag groups, feeds, and news
  store.fetchTagGroup()
  store.fetchFeeds()
  store.fetchNews()
  await handleDeepLink(true)
  window.addEventListener('hashchange', onHashChange)
})

onUnmounted(() => {
  window.removeEventListener('hashchange', onHashChange)
})

// --- Dropdown for Including Tag Groups ---
const dropdownOpen = ref(false)
const toggleDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownOpen.value = !dropdownOpen.value
}

const dropdownShortLabel = computed(() => {
  const count = store.selectedTagGroups.length
  if (count === 0) return 'All'
  if (count === 1) return store.selectedTagGroups[0]
  return `${count} selected`
})

// --- Dropdown for Excluding Tag Groups ---
const dropdownExcludedOpen = ref(false)
const toggleExcludedDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownExcludedOpen.value = !dropdownExcludedOpen.value
}

const dropdownExcludedShortLabel = computed(() => {
  const count = store.excludedTagGroups.length
  if (count === 0) return 'None'
  if (count === 1) return store.excludedTagGroups[0]
  return `${count} selected`
})

// Global function to close all dropdowns
const closeAllDropdowns = () => {
  dropdownOpen.value = false
  dropdownExcludedOpen.value = false
  dropdownFeedOpen.value = false
}

const feedNewsCounts = computed(() => {
  const counts: Record<number, number> = {}
  for (const feed of store.feedEntries) {
    counts[feed.id] = store.newsEntries.filter(entry => entry.feedId === feed.id).length
  }
  return counts
})

const feedDropdownShortLabel = computed(() => {
  const count = store.selectedFeeds.length
  if (count === 0) return 'All'
  if (count === 1) {
    const feed = store.feedEntries.find(f => f.id === store.selectedFeeds[0])
    return feed?.title || 'All'
  }
  return `${count} selected`
})

const dropdownFeedOpen = ref(false)
const toggleFeedDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownFeedOpen.value = !dropdownFeedOpen.value
}

// --- Modal for Login ---
const showModal = ref(false)
const toggleModal = () => {
  showModal.value = !showModal.value
}

const singleNewsViewEntries = computed(() => {
  const entries = store.filteredNews
  const deepEntry = store.deepLinkedNewsEntry
  if (deepEntry && !entries.some(e => e.id === deepEntry.id)) {
    return [deepEntry, ...entries]
  }
  return entries
})

const refContainer = ref<HTMLElement|null>(null)
onClickOutside(refContainer, closeAllDropdowns)
</script>

<template>
  <div class="controls-bar" ref="refContainer">
    <div class="date-nav">
      <button
        @click="changeDate(-1)"
        :disabled="loading"
        class="nav-btn"
        title="Previous day"
      >&larr;</button>
      <span class="current-date" @click="changeDate(0)" title="Go to today">
        {{ store.dateToShowAsDate.toLocaleDateString('de-DE', { weekday: 'short', day: 'numeric', month: 'short' }) }}
      </span>
      <button
        @click="changeDate(1)"
        :disabled="store.isDateToday || loading"
        class="nav-btn"
        title="Next day"
      >&rarr;</button>
    </div>

    <div class="filters">
      <button v-if="!store.loggedIn" @click="toggleModal" class="control-btn" title="Login">
        Login
      </button>

      <div class="dropdown" @click.stop="toggleFeedDropdown">
        <span class="dropdown-label">Feeds</span>
        <span class="dropdown-value">{{ feedDropdownShortLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownFeedOpen ? '▲' : '▼' }}</span>
        <div class="dropdown-menu" v-if="dropdownFeedOpen" @click.stop>
          <label v-for="feed in store.feedEntries" :key="feed.id" class="dropdown-item">
            <input type="checkbox" :value="feed.id" v-model="store.selectedFeeds" />
            {{ feed.title }} ({{ feedNewsCounts[feed.id] || 0 }})
          </label>
        </div>
      </div>

      <div class="dropdown" @click.stop="toggleDropdown">
        <span class="dropdown-label">Include</span>
        <span class="dropdown-value">{{ dropdownShortLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownOpen ? '▲' : '▼' }}</span>
        <div class="dropdown-menu" v-if="dropdownOpen" @click.stop>
          <label v-for="key in store.tagGroupKeys" :key="key" class="dropdown-item">
            <input type="checkbox" :value="key" v-model="store.selectedTagGroups" />
            {{ key }} ({{ store.tagGroupCounts[key] }})
          </label>
        </div>
      </div>

      <div class="dropdown" @click.stop="toggleExcludedDropdown">
        <span class="dropdown-label">Exclude</span>
        <span class="dropdown-value">{{ dropdownExcludedShortLabel }}</span>
        <span class="dropdown-arrow">{{ dropdownExcludedOpen ? '▲' : '▼' }}</span>
        <div class="dropdown-menu" v-if="dropdownExcludedOpen" @click.stop>
          <label v-for="key in store.tagGroupKeys" :key="key" class="dropdown-item">
            <input type="checkbox" :value="key" v-model="store.excludedTagGroups" />
            {{ key }} ({{ store.tagGroupCounts[key] }})
          </label>
        </div>
      </div>
    </div>
  </div>
  <!-- Single News View Mode -->
  <SingleNewsView v-if="store.singleNewsMode && (store.filteredNews.length > 0 || store.deepLinkedNewsEntry)" :newsEntries="singleNewsViewEntries" />

  <!-- Normal 3-section View -->
  <template v-else>
    <div v-if="store.filteredNews.length > 0">
      <NewsSection :newsEntries="store.nightNews" sectionHeader="Night News" :feedEntries="store.feedEntries" />
      <NewsSection :newsEntries="store.afternoonNews" sectionHeader="Afternoon News" :feedEntries="store.feedEntries" />
      <NewsSection :newsEntries="store.morningNews" sectionHeader="Morning News" :feedEntries="store.feedEntries" />
    </div>
    <p v-else>Keine Nachrichten für diesen Tag</p>
  </template>


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
/* Controls Bar */
.controls-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--border-color);
}

/* Date Navigation */
.date-nav {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.nav-btn {
  padding: 0.25rem 0.375rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.75rem;
  line-height: 1;
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
}

.nav-btn:hover:not(:disabled) {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.nav-btn:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
  opacity: 0.5;
}

.current-date {
  font-size: 0.75rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.25rem 0.375rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.current-date:hover {
  background-color: var(--bg-hover);
}

/* Filters */
.filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.25rem;
}

/* Control button (Login) */
.control-btn {
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.75rem;
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
}

.control-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

/* Dropdown styles */
.dropdown {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  user-select: none;
  font-size: 0.75rem;
  transition: border-color 0.2s, background-color 0.2s;
}

.dropdown:hover {
  border-color: var(--border-hover);
}

.dropdown-label {
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.dropdown-value {
  color: var(--text-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  font-size: 0.6rem;
  color: var(--text-muted);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  z-index: 10;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 0.5rem;
  box-shadow: 0 4px 12px var(--shadow-strong);
  max-height: 240px;
  overflow-y: auto;
  min-width: 200px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.5rem;
  cursor: pointer;
  border-radius: 3px;
  transition: background-color 0.15s;
  white-space: nowrap;
  font-size: 0.9rem;
}

.dropdown-item:hover {
  background-color: var(--bg-hover);
}

.dropdown-item input[type="checkbox"] {
  margin: 0;
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
  border: 3px solid var(--spinner-bg);
  border-left-color: var(--spinner-color);
  border-radius: 50%;
  width: 32px;
  height: 32px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: var(--overlay-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: var(--bg-primary);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px var(--shadow-color);
  position: relative;
}

.close-button {
  position: absolute;
  top: 10px;
  right: 20px;
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s, border-color 0.2s;
}
.close-button:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
}

</style>
