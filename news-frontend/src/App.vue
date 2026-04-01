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
      <div class="header-title-area">
        <h1 class="site-title">Lesbare Nachrichten</h1>
        <span class="site-subtitle">Kuratiert & zusammengefasst</span>
      </div>
      <div class="header-right">
        <div v-if="store.loggedIn" class="apps-menu">
          <button class="apps-btn" @click="toggleApps" title="Switch app">
            <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor">
              <circle cx="3" cy="3" r="1.5" />
              <circle cx="8" cy="3" r="1.5" />
              <circle cx="13" cy="3" r="1.5" />
              <circle cx="3" cy="8" r="1.5" />
              <circle cx="8" cy="8" r="1.5" />
              <circle cx="13" cy="8" r="1.5" />
              <circle cx="3" cy="13" r="1.5" />
              <circle cx="8" cy="13" r="1.5" />
              <circle cx="13" cy="13" r="1.5" />
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
    <div class="header-rule"></div>
    <NewsList />
  </main>
</template>

<style scoped>
main {
  max-width: 740px;
  margin: 0 auto;
}

.app-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.header-title-area {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
}

.site-title {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 400;
  margin: 0;
  color: var(--text-primary);
  white-space: nowrap;
  letter-spacing: -0.02em;
  line-height: 1;
}

.site-subtitle {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  color: var(--text-muted);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
}

.header-rule {
  height: 2px;
  background: linear-gradient(90deg, var(--accent-line), var(--accent-line) 30%, var(--border-color) 30%, var(--border-color));
  margin-bottom: 1.25rem;
  border-radius: 1px;
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
  padding: 0.35rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.apps-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.apps-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 150px;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: 0 8px 24px var(--shadow-strong), 0 2px 8px var(--shadow-color);
  z-index: 100;
  padding: 0.375rem;
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

.app-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0.625rem;
  color: var(--text-primary);
  text-decoration: none;
  font-family: var(--font-ui);
  font-size: 0.78rem;
  font-weight: 500;
  border-radius: 5px;
  transition: background-color 0.15s;
}

.app-link:hover {
  background-color: var(--bg-hover);
  text-decoration: none;
}

.app-current {
  color: var(--text-muted);
}

.current-badge {
  font-size: 0.575rem;
  color: var(--primary-color);
  margin-left: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 600;
}

@media (max-width: 480px) {
  .site-title {
    font-size: 1.2rem;
  }

  .site-subtitle {
    display: none;
  }

  .header-title-area {
    gap: 0.5rem;
  }
}
</style>
