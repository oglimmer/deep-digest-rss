<script setup lang="ts">
const emit = defineEmits<{ close: [] }>()

defineProps<{
  width?: string
  maxWidth?: string
  padding?: string
}>()
</script>

<template>
  <div class="modal-overlay" @click="emit('close')">
    <div
      class="modal-content"
      :style="{
        width: width,
        maxWidth: maxWidth,
        padding: padding,
      }"
      @click.stop
    >
      <slot />
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: var(--overlay-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.modal-content {
  background-color: var(--bg-primary);
  padding: 0;
  border-radius: 12px;
  box-shadow: 0 16px 48px var(--shadow-strong);
  position: relative;
  animation: modalReveal 0.2s ease;
}

@keyframes modalReveal {
  from {
    opacity: 0;
    transform: scale(0.96) translateY(8px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
