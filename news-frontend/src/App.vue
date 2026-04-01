<script setup lang="ts">
import { onMounted, ref } from 'vue';
import NewsList from './components/NewsList.vue';
import ThemeToggle from './components/ThemeToggle.vue';
import { useDataStore } from '@/stores/data';

const store = useDataStore();
const appsOpen = ref(false);

const apps = [
  { name: 'Content', url: 'https://content.oglimmer.com/' },
  { name: 'Infographics', url: 'https://infographics.oglimmer.com/' },
  { name: 'News', url: 'https://news.oglimmer.com/', current: true },
  { name: 'Linky', url: 'https://www.linky1.com/' },
];

const toggleApps = () => {
  appsOpen.value = !appsOpen.value;
};

const onClickOutside = (e: MouseEvent) => {
  const target = e.target as HTMLElement;
  if (appsOpen.value && !target.closest('.apps-menu')) {
    appsOpen.value = false;
  }
};

onMounted(() => {
  store.initTheme();
  document.addEventListener('click', onClickOutside);
});
</script>

<template>
  <main>
    <header class="app-header">
      <h1 class="site-title">Lesbare Nachrichten</h1>
      <div class="header-right">
        <div v-if="store.loggedIn" class="apps-menu">
          <button class="apps-btn" @click="toggleApps" title="Switch app">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
              <rect x="1" y="1" width="4" height="4" rx="0.5" />
              <rect x="6" y="1" width="4" height="4" rx="0.5" />
              <rect x="11" y="1" width="4" height="4" rx="0.5" />
              <rect x="1" y="6" width="4" height="4" rx="0.5" />
              <rect x="6" y="6" width="4" height="4" rx="0.5" />
              <rect x="11" y="6" width="4" height="4" rx="0.5" />
              <rect x="1" y="11" width="4" height="4" rx="0.5" />
              <rect x="6" y="11" width="4" height="4" rx="0.5" />
              <rect x="11" y="11" width="4" height="4" rx="0.5" />
            </svg>
          </button>
          <div v-if="appsOpen" class="apps-dropdown">
            <a
              v-for="app in apps"
              :key="app.url"
              :href="app.url"
              class="app-link"
              :class="{ 'app-current': app.current }"
              target="_blank"
              rel="noopener"
            >
              {{ app.name }}
              <span v-if="app.current" class="current-badge">current</span>
            </a>
          </div>
        </div>
        <ThemeToggle />
      </div>
    </header>
    <NewsList />
  </main>
</template>

<style scoped>
main {
  max-width: 720px;
  margin: 0 auto;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.site-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.apps-menu {
  position: relative;
}

.apps-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  transition: background-color 0.2s, border-color 0.2s;
}

.apps-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
}

.apps-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  min-width: 140px;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
  padding: 0.25rem 0;
}

.app-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0.75rem;
  color: var(--text-primary);
  text-decoration: none;
  font-size: 0.8rem;
  transition: background-color 0.15s;
}

.app-link:hover {
  background-color: var(--bg-hover);
}

.app-current {
  color: var(--text-muted);
}

.current-badge {
  font-size: 0.625rem;
  color: var(--text-muted);
  margin-left: 0.5rem;
}

@media (max-width: 480px) {
  .site-title {
    font-size: 1rem;
  }
}
</style>
