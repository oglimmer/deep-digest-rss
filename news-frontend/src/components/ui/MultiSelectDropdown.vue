<script setup lang="ts" generic="T extends string | number">
import { computed } from 'vue'

interface Option {
  value: T
  label: string
  count?: number
}

const props = defineProps<{
  label: string
  options: Option[]
  modelValue: T[]
  open: boolean
  emptyLabel?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: T[]]
  'update:open': [value: boolean]
}>()

const selected = computed({
  get: () => props.modelValue,
  set: (v: T[]) => emit('update:modelValue', v),
})

const shortLabel = computed(() => {
  const count = props.modelValue.length
  if (count === 0) return props.emptyLabel ?? 'All'
  if (count === 1) {
    const match = props.options.find((o) => o.value === props.modelValue[0])
    return match?.label ?? String(props.modelValue[0])
  }
  return `${count} selected`
})

const toggle = (event: MouseEvent) => {
  event.stopPropagation()
  emit('update:open', !props.open)
}
</script>

<template>
  <div class="dropdown" @click.stop="toggle">
    <span class="dropdown-label">{{ label }}</span>
    <span class="dropdown-value">{{ shortLabel }}</span>
    <svg
      class="dropdown-chevron"
      :class="{ open }"
      width="8" height="8" viewBox="0 0 8 8"
      fill="none" stroke="currentColor" stroke-width="1.2"
    >
      <path d="M1 2.5L4 5.5L7 2.5" />
    </svg>
    <div class="dropdown-menu" v-if="open" @click.stop>
      <label v-for="opt in options" :key="opt.value" class="dropdown-item">
        <input type="checkbox" :value="opt.value" v-model="selected" />
        <span class="dropdown-item-text">{{ opt.label }}</span>
        <span v-if="opt.count !== undefined" class="dropdown-item-count">{{ opt.count }}</span>
      </label>
    </div>
  </div>
</template>

<style scoped>
.dropdown {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.35rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--text-primary);
  cursor: pointer;
  user-select: none;
  font-family: var(--font-ui);
  font-size: 0.7rem;
  transition: all 0.2s ease;
}

.dropdown:hover {
  border-color: var(--border-hover);
  background-color: var(--bg-hover);
}

.dropdown-label {
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-size: 0.6rem;
  font-weight: 600;
}

.dropdown-value {
  color: var(--text-secondary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.dropdown-chevron {
  color: var(--text-muted);
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.dropdown-chevron.open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 10;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.375rem;
  box-shadow: 0 8px 24px var(--shadow-strong), 0 2px 8px var(--shadow-color);
  max-height: 260px;
  overflow-y: auto;
  min-width: 220px;
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

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.5rem;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.15s;
  white-space: nowrap;
  font-size: 0.8rem;
  font-weight: 400;
}

.dropdown-item:hover {
  background-color: var(--bg-hover);
}

.dropdown-item input[type="checkbox"] {
  margin: 0;
  accent-color: var(--primary-color);
}

.dropdown-item-text {
  flex: 1;
}

.dropdown-item-count {
  font-size: 0.7rem;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}
</style>
