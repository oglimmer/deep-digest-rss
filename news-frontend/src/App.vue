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
    <ThemeToggle />
    <NewsList />
  </main>
</template>

<style scoped>

</style>
