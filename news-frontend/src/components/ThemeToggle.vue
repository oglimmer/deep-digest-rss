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

    <!-- Font size controls -->
    <div class="font-size-controls">
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

    <!-- Dark mode toggle -->
    <button
      class="theme-toggle"
      @click="store.toggleDarkMode()"
      :title="store.darkMode ? 'Switch to light mode' : 'Switch to dark mode'"
    >
      <span v-if="store.darkMode">&#9728;</span>
      <span v-else>&#9790;</span>
    </button>
  </div>
</template>

<style scoped>
.settings-bar {
  position: fixed;
  top: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 100;
}

.font-select {
  padding: 6px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 0.9rem;
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
  gap: 4px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  padding: 2px;
}

.size-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 3px;
  background-color: transparent;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 0.85rem;
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
  min-width: 24px;
  text-align: center;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.theme-toggle {
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 1.2rem;
  transition: background-color 0.2s, border-color 0.2s;
}

.theme-toggle:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
}
</style>
