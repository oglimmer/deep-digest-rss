<script setup lang="ts">
import { useUiStore } from '@/stores/ui'

const props = defineProps<{ loading: boolean }>()
const emit = defineEmits<{ change: [days: number] }>()

const ui = useUiStore()

const handle = (days: number) => {
  emit('change', days)
}

void props
</script>

<template>
  <div class="date-nav">
    <button
      @click="handle(-1)"
      :disabled="loading"
      class="nav-btn"
      title="Previous day"
    >
      <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
        <path d="M7.5 2.5L4 6L7.5 9.5" />
      </svg>
    </button>
    <span class="current-date" @click="handle(0)" title="Go to today">
      {{ ui.dateAsDate.toLocaleDateString('de-DE', { weekday: 'short', day: 'numeric', month: 'short' }) }}
    </span>
    <button
      @click="handle(1)"
      :disabled="ui.isDateToday || loading"
      class="nav-btn"
      title="Next day"
    >
      <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
        <path d="M4.5 2.5L8 6L4.5 9.5" />
      </svg>
    </button>
  </div>
</template>

<style scoped>
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
</style>
