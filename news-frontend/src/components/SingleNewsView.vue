<script setup lang="ts">
import type { NewsEntry } from '@/interfaces'
import { useDataStore } from '@/stores/data'

const store = useDataStore()

defineProps<{
  newsEntries: NewsEntry[]
}>()
</script>

<template>
  <div class="single-news-container">
    <button
      class="exit-btn"
      @click="store.toggleSingleNewsMode()"
      title="Exit single article view"
    >
      ✕
    </button>
    <article v-for="entry in newsEntries" :key="entry.id" class="single-article">
      <div class="article-content">
        <h2>{{ entry.title }}</h2>
        <p>{{ entry.text }}</p>
      </div>
    </article>
  </div>
</template>

<style scoped>
.single-news-container {
  height: 100vh;
  overflow-y: scroll;
  scroll-snap-type: y mandatory;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--bg-primary);
  z-index: 50;
}

.exit-btn {
  position: fixed;
  top: 0.75rem;
  right: 0.75rem;
  z-index: 60;
  width: 2rem;
  height: 2rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
}

.exit-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.single-article {
  min-height: 100vh;
  max-height: 100vh;
  scroll-snap-align: start;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  box-sizing: border-box;
  overflow: hidden;
}

.article-content {
  max-width: 600px;
  width: 100%;
  text-align: left;
  max-height: calc(100vh - 2rem);
  overflow: hidden;
}

.article-content h2 {
  font-size: calc(var(--font-size-base) * 1.3);
  margin-bottom: 1rem;
  line-height: 1.3;
  color: var(--text-primary);
}

.article-content p {
  font-size: var(--font-size-base);
  line-height: 1.6;
  color: var(--text-secondary);
}

@media (max-width: 480px) {
  .article-content h2 {
    font-size: calc(var(--font-size-base) * 1.2);
  }

  .article-content p {
    font-size: var(--font-size-base);
    line-height: 1.55;
  }
}
</style>
