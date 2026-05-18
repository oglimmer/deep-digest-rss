<script setup lang="ts">
const props = defineProps<{
  text: string
  id: string
  copied: boolean
}>()

const emit = defineEmits<{ copy: [text: string, id: string] }>()

const handle = () => emit('copy', props.text, props.id)
</script>

<template>
  <button class="copy-btn" :class="{ copied }" @click="handle">
    {{ copied ? '✓' : 'copy' }}
  </button>
</template>
