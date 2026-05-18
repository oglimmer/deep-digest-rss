<script setup lang="ts">
defineProps<{ fixed?: boolean }>()
</script>

<template>
  <div :class="['spinner-wrap', { 'spinner-fixed': fixed }]">
    <div class="spinner"></div>
  </div>
</template>

<style scoped>
.spinner-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
}

.spinner-fixed {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
}

.spinner {
  border: 2px solid var(--spinner-bg);
  border-left-color: var(--spinner-color);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  animation: spin 0.8s linear infinite;
}
</style>
