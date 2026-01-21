<script setup lang="ts">
import type { NewsEntry } from '@/interfaces'

defineProps<{
  newsEntries: NewsEntry[]
}>()
</script>

<template>
  <div class="single-news-container">
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

.single-article {
  min-height: 100vh;
  scroll-snap-align: start;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  box-sizing: border-box;
}

.article-content {
  max-width: 700px;
  width: 100%;
  text-align: left;
}

.article-content h2 {
  font-size: 1.75rem;
  margin-bottom: 1.5rem;
  line-height: 1.3;
  color: var(--text-primary);
}

.article-content p {
  font-size: 1.125rem;
  line-height: 1.8;
  color: var(--text-secondary);
}
</style>
