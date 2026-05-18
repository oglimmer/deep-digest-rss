<script setup lang="ts">
import CopyButton from './CopyButton.vue'

const props = defineProps<{
  content: string
  id?: string
  copyable?: boolean
  copied?: boolean
}>()

const emit = defineEmits<{ copy: [text: string, id: string] }>()

const onCopy = (text: string, id: string) => emit('copy', text, id)

void props
</script>

<template>
  <div class="code-block">
    <pre>{{ content }}</pre>
    <CopyButton
      v-if="copyable && id"
      :text="content"
      :id="id"
      :copied="!!copied"
      @copy="onCopy"
    />
  </div>
</template>
