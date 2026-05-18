<script setup lang="ts">
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseSpinner from '@/components/ui/BaseSpinner.vue'
import { useDigestStore } from '@/stores/digest'
import { renderMarkdown } from '@/composables/useMarkdownRenderer'

defineProps<{ loading: boolean }>()
const emit = defineEmits<{ close: [] }>()

const digest = useDigestStore()

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('de-DE', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
</script>

<template>
  <BaseModal width="90%" max-width="780px" padding="2rem" @close="emit('close')">
    <button class="digest-close" @click="emit('close')">&times;</button>
    <div v-if="loading" class="digest-loading">
      <BaseSpinner />
    </div>
    <div v-else-if="digest.current" class="digest-body">
      <div class="digest-meta">{{ formatDate(digest.current.createdOn) }}</div>
      <div class="digest-content" v-html="renderMarkdown(digest.current.content)"></div>
    </div>
    <div v-else class="digest-empty">
      Kein Digest f&uuml;r diesen Tag verf&uuml;gbar.
    </div>
  </BaseModal>
</template>

<style scoped>
.digest-close {
  position: absolute;
  top: 0.75rem;
  right: 1rem;
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s;
}

.digest-close:hover {
  color: var(--text-primary);
}

.digest-loading {
  display: flex;
  justify-content: center;
  padding: 3rem 0;
}

.digest-meta {
  font-family: var(--font-ui);
  font-size: 0.7rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--border-color);
}

.digest-content {
  font-size: var(--font-size-base);
  line-height: 1.7;
  color: var(--text-primary);
}

.digest-content :deep(h2) {
  font-family: var(--font-display);
  font-size: 1.3em;
  font-weight: 700;
  margin: 1.5rem 0 0.75rem;
  color: var(--text-primary);
}

.digest-content :deep(h3) {
  font-family: var(--font-display);
  font-size: 1.1em;
  font-weight: 600;
  margin: 1.25rem 0 0.5rem;
  color: var(--text-secondary);
}

.digest-content :deep(p) {
  margin: 0.5rem 0;
}

.digest-content :deep(strong) {
  color: var(--text-primary);
}

.digest-empty {
  text-align: center;
  color: var(--text-muted);
  font-family: var(--font-ui);
  font-size: 0.85rem;
  padding: 3rem 1rem;
  font-style: italic;
}
</style>
