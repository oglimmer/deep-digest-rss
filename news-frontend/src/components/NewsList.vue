<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onClickOutside } from '@vueuse/core'
import NewsSection from './NewsSection.vue'
import LoginForm from './LoginForm.vue'
import { useDataStore } from '@/stores/data'

const store = useDataStore()

// const excludeAds = ref(dataStore.excludeAds)
const loading = ref(false)

const changeDate = async (days: number) => {
  loading.value = true
  await store.changeDate(days)
  loading.value = false
}

onMounted(async () => {
  // Fetch initial tag groups, feeds, and news
  store.fetchTagGroup()
  store.fetchFeeds()
  store.fetchNews()
})

// --- Dropdown for Including Tag Groups ---
const dropdownOpen = ref(false)
const toggleDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownOpen.value = !dropdownOpen.value
}

const dropdownLabel = computed(() => {
  return store.selectedTagGroups.length > 0
    ? store.selectedTagGroups.join(', ')
    : 'none'
})

// --- Dropdown for Excluding Tag Groups ---
const dropdownExcludedOpen = ref(false)
const toggleExcludedDropdown = (event: MouseEvent) => {
  event.stopPropagation()
  dropdownExcludedOpen.value = !dropdownExcludedOpen.value
}

const dropdownExcludedLabel = computed(() => {
  return store.excludedTagGroups.length > 0
    ? store.excludedTagGroups.join(', ')
    : 'none'
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

const feedDropdownLabel = computed(() => {
  return store.selectedFeeds.length > 0
    ? store.selectedFeeds.map(id =>
        store.feedEntries.find(feed => feed.id === id)?.title
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

const refContainer = ref<HTMLElement|null>(null)
onClickOutside(refContainer, closeAllDropdowns)
</script>

<template>
  <div>
    <h2 @click="changeDate(0)">
      Lesbare Nachrichten für den {{ store.dateToShowAsDate.toLocaleDateString() }}
    </h2>
    <!-- Login Button -->
    <button v-if="!store.loggedIn" @click="toggleModal" class="login-button">L</button>

    <!-- Custom styled feed select with a typical arrow -->
    <span ref="refContainer">
      <div class="dropdown" @click.stop="toggleFeedDropdown">
        <div class="dropdown-header">
          <span>{{ feedDropdownLabel }}</span>
          <span class="dropdown-arrow">{{ dropdownFeedOpen ? '▲' : '▼' }}</span>
        </div>
        <div class="dropdown-menu" v-if="dropdownFeedOpen" @click.stop>
          <label v-for="feed in store.feedEntries" :key="feed.id" class="dropdown-item">
            <input type="checkbox" :value="feed.id" v-model="store.selectedFeeds" />
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
          <label v-for="key in store.tagGroupKeys" :key="key" class="dropdown-item">
            <input type="checkbox" :value="key" v-model="store.selectedTagGroups" />
            {{ key }} ({{ store.tagGroupCounts[key] }})
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
          <label v-for="key in store.tagGroupKeys" :key="key" class="dropdown-item">
            <input type="checkbox" :value="key" v-model="store.excludedTagGroups" />
            {{ key }} ({{ store.tagGroupCounts[key] }})
          </label>
        </div>
      </div>
    </span>
  </div>
  <div v-if="store.filteredNews.length > 0">
    <NewsSection :newsEntries="store.nightNews" sectionHeader="Night News" :feedEntries="store.feedEntries" />
    <NewsSection :newsEntries="store.afternoonNews" sectionHeader="Afternoon News" :feedEntries="store.feedEntries" />
    <NewsSection :newsEntries="store.morningNews" sectionHeader="Morning News" :feedEntries="store.feedEntries" />
  </div>
  <p v-else>Keine Nachrichten für diesen Tag</p>

  <div class="control-wrapper">
    <button @click="changeDate(-1)" :disabled="loading" class="custom-button">Previous Day</button>
    <button @click="changeDate(1)" :disabled="store.isDateToday" class="custom-button">Next Day</button>
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
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
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
  background-color: var(--bg-secondary);
  border-color: var(--border-hover);
}

/* Custom button styling */
.custom-button {
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 1rem;
  margin-right: 8px;
  min-width: 100px;
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
}
.custom-button:hover:not(:disabled) {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
}
.custom-button:disabled {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-muted);
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
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  user-select: none;
  min-width: 280px;
  transition: border-color 0.2s, background-color 0.2s;
}
.dropdown:hover {
  border-color: var(--border-hover);
}
.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  white-space: nowrap;
}
.dropdown-arrow {
  margin-left: 8px;
}
.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 10;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 8px;
  box-shadow: 0 2px 8px var(--shadow-strong);
  max-height: 200px;
  overflow-y: auto;
  min-width: 280px;
}
.dropdown-item {
  display: block;
  padding: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
}
.dropdown-item:hover {
  background-color: var(--bg-hover);
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
  border: 4px solid var(--spinner-bg);
  border-left-color: var(--spinner-color);
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
  right: 10px;
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

.login-button {
  position: fixed;
  top: 10px;
  right: 240px;
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s, border-color 0.2s;
  z-index: 100;
}
.login-button:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
}
</style>
