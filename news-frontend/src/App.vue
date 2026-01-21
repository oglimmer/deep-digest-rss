<script setup lang="ts">
import { onMounted } from 'vue';
import NewsList from './components/NewsList.vue';
import ThemeToggle from './components/ThemeToggle.vue';
import { useDataStore } from '@/stores/data';

const store = useDataStore();

onMounted(() => {
  store.initTheme();
});
</script>

<template>
  <main>
    <header class="app-header">
      <h1 class="site-title">Lesbare Nachrichten</h1>
      <ThemeToggle />
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

@media (max-width: 480px) {
  .site-title {
    font-size: 1rem;
  }
}
</style>
