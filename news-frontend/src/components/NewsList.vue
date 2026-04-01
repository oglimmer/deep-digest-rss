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
      >
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
          <path d="M7.5 2.5L4 6L7.5 9.5" />
        </svg>
      </button>
      <span class="current-date" @click="changeDate(0)" title="Go to today">
        {{ store.dateToShowAsDate.toLocaleDateString('de-DE', { weekday: 'short', day: 'numeric', month: 'short' }) }}
      </span>
      <button
        @click="changeDate(1)"
        :disabled="store.isDateToday || loading"
        class="nav-btn"
        title="Next day"
      >
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
          <path d="M4.5 2.5L8 6L4.5 9.5" />
        </svg>
      </button>
    </div>

    <div class="filters">
      <button v-if="!store.loggedIn" @click="toggleModal" class="control-btn" title="Login">
        Login
      </button>

      <div class="dropdown" @click.stop="toggleFeedDropdown">
        <span class="dropdown-label">Feeds</span>
        <span class="dropdown-value">{{ feedDropdownShortLabel }}</span>
        <svg class="dropdown-chevron" :class="{ open: dropdownFeedOpen }" width="8" height="8" viewBox="0 0 8 8" fill="none" stroke="currentColor" stroke-width="1.2"><path d="M1 2.5L4 5.5L7 2.5" /></svg>
        <div class="dropdown-menu" v-if="dropdownFeedOpen" @click.stop>
          <label v-for="feed in store.feedEntries" :key="feed.id" class="dropdown-item">
            <input type="checkbox" :value="feed.id" v-model="store.selectedFeeds" />
            <span class="dropdown-item-text">{{ feed.title }}</span>
            <span class="dropdown-item-count">{{ feedNewsCounts[feed.id] || 0 }}</span>
          </label>
        </div>
      </div>

      <div class="dropdown" @click.stop="toggleDropdown">
        <span class="dropdown-label">Include</span>
        <span class="dropdown-value">{{ dropdownShortLabel }}</span>
        <svg class="dropdown-chevron" :class="{ open: dropdownOpen }" width="8" height="8" viewBox="0 0 8 8" fill="none" stroke="currentColor" stroke-width="1.2"><path d="M1 2.5L4 5.5L7 2.5" /></svg>
        <div class="dropdown-menu" v-if="dropdownOpen" @click.stop>
          <label v-for="key in store.tagGroupKeys" :key="key" class="dropdown-item">
            <input type="checkbox" :value="key" v-model="store.selectedTagGroups" />
            <span class="dropdown-item-text">{{ key }}</span>
            <span class="dropdown-item-count">{{ store.tagGroupCounts[key] }}</span>
          </label>
        </div>
      </div>

      <div class="dropdown" @click.stop="toggleExcludedDropdown">
        <span class="dropdown-label">Exclude</span>
        <span class="dropdown-value">{{ dropdownExcludedShortLabel }}</span>
        <svg class="dropdown-chevron" :class="{ open: dropdownExcludedOpen }" width="8" height="8" viewBox="0 0 8 8" fill="none" stroke="currentColor" stroke-width="1.2"><path d="M1 2.5L4 5.5L7 2.5" /></svg>
        <div class="dropdown-menu" v-if="dropdownExcludedOpen" @click.stop>
          <label v-for="key in store.tagGroupKeys" :key="key" class="dropdown-item">
            <input type="checkbox" :value="key" v-model="store.excludedTagGroups" />
            <span class="dropdown-item-text">{{ key }}</span>
            <span class="dropdown-item-count">{{ store.tagGroupCounts[key] }}</span>
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
    <p v-else class="empty-state">Keine Nachrichten f&uuml;r diesen Tag</p>
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
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
}

/* Date Navigation */
.date-nav {
  display: flex;
  align-items: center;
  gap: 0.125rem;
}

.nav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.75rem;
  height: 1.75rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-btn:hover:not(:disabled) {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.nav-btn:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
  opacity: 0.3;
}

.current-date {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.35rem 0.625rem;
  border-radius: 6px;
  transition: all 0.2s ease;
  letter-spacing: 0.01em;
}

.current-date:hover {
  background-color: var(--bg-hover);
  color: var(--text-primary);
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
  padding: 0.35rem 0.75rem;
  border: 1px solid var(--primary-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--primary-color);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.03em;
  transition: all 0.2s ease;
}

.control-btn:hover {
  background-color: var(--primary-color);
  color: #fff;
}

/* Dropdown styles */
.dropdown {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.35rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--text-primary);
  cursor: pointer;
  user-select: none;
  font-family: var(--font-ui);
  font-size: 0.7rem;
  transition: all 0.2s ease;
}

.dropdown:hover {
  border-color: var(--border-hover);
  background-color: var(--bg-hover);
}

.dropdown-label {
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-size: 0.6rem;
  font-weight: 600;
}

.dropdown-value {
  color: var(--text-secondary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.dropdown-chevron {
  color: var(--text-muted);
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.dropdown-chevron.open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 10;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.375rem;
  box-shadow: 0 8px 24px var(--shadow-strong), 0 2px 8px var(--shadow-color);
  max-height: 260px;
  overflow-y: auto;
  min-width: 220px;
  animation: dropdownReveal 0.15s ease;
}

@keyframes dropdownReveal {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.5rem;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.15s;
  white-space: nowrap;
  font-size: 0.8rem;
  font-weight: 400;
}

.dropdown-item:hover {
  background-color: var(--bg-hover);
}

.dropdown-item input[type="checkbox"] {
  margin: 0;
  accent-color: var(--primary-color);
}

.dropdown-item-text {
  flex: 1;
}

.dropdown-item-count {
  font-size: 0.7rem;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}

/* Empty state */
.empty-state {
  text-align: center;
  color: var(--text-muted);
  font-family: var(--font-ui);
  font-size: 0.85rem;
  padding: 3rem 1rem;
  font-style: italic;
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
  border: 2px solid var(--spinner-bg);
  border-left-color: var(--spinner-color);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  animation: spin 0.8s linear infinite;
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
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.modal-content {
  background-color: var(--bg-primary);
  padding: 0;
  border-radius: 12px;
  box-shadow: 0 16px 48px var(--shadow-strong);
  position: relative;
  animation: modalReveal 0.2s ease;
}

@keyframes modalReveal {
  from {
    opacity: 0;
    transform: scale(0.96) translateY(8px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

</style>
