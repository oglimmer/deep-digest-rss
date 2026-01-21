<script setup lang="ts">
import { useDataStore } from '@/stores/data';

const store = useDataStore();

const fontOptions = [
  { value: 'system', label: 'System' },
  { value: 'georgia', label: 'Georgia' },
  { value: 'palatino', label: 'Palatino' },
  { value: 'charter', label: 'Charter' },
  { value: 'verdana', label: 'Verdana' }
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
      {{ store.singleNewsMode ? '☰' : '▢' }}
    </button>
  </div>
</template>

<style scoped>
.settings-bar {
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.font-select {
  padding: 0.25rem 0.375rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 0.75rem;
  transition: background-color 0.2s, border-color 0.2s;
}

.font-select:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
}

.font-select:focus {
  outline: none;
  border-color: var(--primary-color);
}

.font-size-controls {
  display: flex;
  align-items: center;
  gap: 2px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  padding: 1px;
}

.size-btn {
  padding: 0.25rem 0.375rem;
  border: none;
  border-radius: 3px;
  background-color: transparent;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 0.7rem;
  font-weight: 600;
  transition: background-color 0.2s;
}

.size-btn:hover:not(:disabled) {
  background-color: var(--bg-hover);
}

.size-btn:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
}

.font-size-display {
  min-width: 1.25rem;
  text-align: center;
  font-size: 0.7rem;
  color: var(--text-secondary);
}

.theme-toggle,
.view-toggle {
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 0.875rem;
  line-height: 1;
  transition: background-color 0.2s, border-color 0.2s;
}

.theme-toggle:hover,
.view-toggle:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
}

.font-size-cycle {
  display: none;
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 600;
  line-height: 1;
  transition: background-color 0.2s, border-color 0.2s;
}

.font-size-cycle:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
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
