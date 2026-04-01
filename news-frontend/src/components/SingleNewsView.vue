<script setup lang="ts">
import type { NewsEntry } from '@/interfaces'
import { useDataStore } from '@/stores/data'
import { onMounted, onUnmounted, ref, nextTick } from 'vue'

const store = useDataStore()
const containerRef = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

defineProps<{
  newsEntries: NewsEntry[]
}>()

onMounted(async () => {
  await nextTick()
  const container = containerRef.value
  if (!container) return

  // Scroll to deep-linked article
  if (store.deepLinkedNewsId) {
    const el = container.querySelector(`[data-article-id="${store.deepLinkedNewsId}"]`)
    if (el) {
      el.scrollIntoView()
    }
  }

  // Track visible article and update hash
  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          const id = (entry.target as HTMLElement).dataset.articleId
          if (id) {
            window.location.hash = `#article/${id}`
          }
        }
      }
    },
    { root: container, threshold: 0.5 }
  )

  container.querySelectorAll('[data-article-id]').forEach((el) => {
    observer!.observe(el)
  })
})

onUnmounted(() => {
  observer?.disconnect()
  if (!store.deepLinkedNewsId) {
    window.location.hash = ''
  }
})
</script>

<template>
  <div class="single-news-container" ref="containerRef">
    <button
      class="exit-btn"
      @click="store.toggleSingleNewsMode()"
      title="Exit single article view"
    >
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
        <path d="M4 4L12 12M12 4L4 12" />
      </svg>
    </button>
    <article v-for="entry in newsEntries" :key="entry.id" class="single-article" :data-article-id="entry.id">
      <div class="article-content">
        <div class="article-meta">
          <span class="article-number">&#167;</span>
        </div>
        <h2>{{ entry.title }}</h2>
        <p>{{ entry.text }}</p>
        <a :href="entry.url" target="_blank" rel="noopener" class="article-link">
          Read source
          <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round">
            <path d="M3 1H9V7M9 1L1 9" />
          </svg>
        </a>
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
  top: 1rem;
  right: 1.5rem;
  z-index: 60;
  width: 2.25rem;
  height: 2.25rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: var(--bg-primary);
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px var(--shadow-color);
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
  padding: 2rem 1.5rem;
  box-sizing: border-box;
  overflow: hidden;
}

.article-content {
  max-width: 580px;
  width: 100%;
  text-align: left;
  max-height: calc(100vh - 4rem);
  overflow: hidden;
}

.article-meta {
  margin-bottom: 1rem;
}

.article-number {
  font-family: var(--font-display);
  font-size: 1.5rem;
  color: var(--primary-color);
  opacity: 0.4;
}

.article-content h2 {
  font-family: var(--font-display);
  font-size: calc(var(--font-size-base) * 1.4);
  font-weight: 400;
  margin: 0 0 1.25rem 0;
  line-height: 1.3;
  letter-spacing: -0.02em;
  color: var(--text-primary);
}

.article-content p {
  font-size: var(--font-size-base);
  line-height: 1.7;
  color: var(--text-secondary);
  margin: 0;
}

.article-link {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  margin-top: 1.75rem;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--primary-color);
  text-decoration: none;
  opacity: 0.6;
  transition: opacity 0.2s ease;
  letter-spacing: 0.02em;
}

.article-link:hover {
  opacity: 1;
  text-decoration: none;
}

@media (max-width: 480px) {
  .article-content h2 {
    font-size: calc(var(--font-size-base) * 1.2);
  }

  .article-content p {
    font-size: var(--font-size-base);
    line-height: 1.6;
  }

  .single-article {
    padding: 1.5rem 1rem;
  }
}
</style>
