<script setup lang="ts">
import { useDataStore } from '@/stores/data';

const store = useDataStore();

const fontOptions = [
  { value: 'system', label: 'Editorial' },
  { value: 'georgia', label: 'Classic' },
  { value: 'palatino', label: 'Elegant' },
  { value: 'charter', label: 'Reader' },
  { value: 'verdana', label: 'Modern' }
] as const;

const decreaseFontSize = () => {
  store.setFontSize(store.fontSize - 1);
};

const increaseFontSize = () => {
  store.setFontSize(store.fontSize + 1);
};

const cycleFontSize = () => {
  if (store.fontSize >= 24) {
    store.setFontSize(12);
  } else {
    store.setFontSize(store.fontSize + 2);
  }
};
</script>

<template>
  <div class="settings-bar">
    <!-- Font selector -->
    <select
      class="font-select"
      :value="store.fontFamily"
      @change="store.setFontFamily(($event.target as HTMLSelectElement).value as 'system' | 'georgia' | 'palatino' | 'charter' | 'verdana')"
      title="Select font family"
    >
      <option v-for="opt in fontOptions" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>

    <!-- Font size controls (desktop) -->
    <div v-if="!store.singleNewsMode" class="font-size-controls">
      <button
        class="size-btn"
        @click="decreaseFontSize"
        :disabled="store.fontSize <= 12"
        title="Decrease font size"
      >
        A-
      </button>
      <span class="font-size-display">{{ store.fontSize }}</span>
      <button
        class="size-btn"
        @click="increaseFontSize"
        :disabled="store.fontSize >= 24"
        title="Increase font size"
      >
        A+
      </button>
    </div>

    <!-- Font size cycle button (mobile only) -->
    <button
      class="font-size-cycle"
      @click="cycleFontSize"
      title="Change font size"
    >
      A
    </button>

    <!-- Dark mode toggle -->
    <button
      class="theme-toggle"
      @click="store.toggleDarkMode()"
      :title="store.darkMode ? 'Switch to light mode' : 'Switch to dark mode'"
    >
      <span v-if="store.darkMode">&#9728;</span>
      <span v-else>&#9790;</span>
    </button>

    <!-- Single news mode toggle -->
    <button
      class="view-toggle"
      @click="store.toggleSingleNewsMode()"
      :title="store.singleNewsMode ? 'Switch to normal view' : 'Switch to single article view'"
    >
      {{ store.singleNewsMode ? '&#9776;' : '&#9634;' }}
    </button>
  </div>
</template>

<style scoped>
.settings-bar {
  display: flex;
  align-items: center;
  gap: 0.325rem;
}

.font-select {
  padding: 0.3rem 0.4rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 500;
  transition: all 0.2s ease;
  appearance: none;
  -webkit-appearance: none;
  padding-right: 1.2rem;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8' viewBox='0 0 8 8'%3E%3Cpath d='M1 2.5L4 5.5L7 2.5' stroke='%239B8E7E' fill='none' stroke-width='1.2'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.35rem center;
}

[data-theme="dark"] .font-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8' viewBox='0 0 8 8'%3E%3Cpath d='M1 2.5L4 5.5L7 2.5' stroke='%237A7062' fill='none' stroke-width='1.2'/%3E%3C/svg%3E");
}

.font-select:hover {
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.font-select:focus {
  outline: none;
  border-color: var(--primary-color);
}

.font-size-controls {
  display: flex;
  align-items: center;
  gap: 1px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  padding: 1px;
  overflow: hidden;
}

.size-btn {
  padding: 0.275rem 0.4rem;
  border: none;
  border-radius: 4px;
  background-color: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  transition: all 0.2s ease;
}

.size-btn:hover:not(:disabled) {
  background-color: var(--bg-hover);
  color: var(--text-primary);
}

.size-btn:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
  opacity: 0.4;
}

.font-size-display {
  min-width: 1.25rem;
  text-align: center;
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 500;
  color: var(--text-muted);
}

.theme-toggle,
.view-toggle {
  padding: 0.275rem 0.45rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.85rem;
  line-height: 1;
  transition: all 0.2s ease;
}

.theme-toggle:hover,
.view-toggle:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.font-size-cycle {
  display: none;
  padding: 0.275rem 0.45rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 600;
  line-height: 1;
  transition: all 0.2s ease;
}

.font-size-cycle:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

@media (max-width: 480px) {
  .font-size-controls {
    display: none;
  }

  .font-size-cycle {
    display: block;
  }
}
</style>
