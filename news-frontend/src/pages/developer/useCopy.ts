import { ref } from 'vue'

export const useCopy = (resetMs = 1800) => {
  const copiedId = ref<string | null>(null)

  const copy = async (text: string, id: string) => {
    await navigator.clipboard.writeText(text.trim())
    copiedId.value = id
    setTimeout(() => {
      if (copiedId.value === id) copiedId.value = null
    }, resetMs)
  }

  const isCopied = (id: string) => copiedId.value === id

  return { copiedId, copy, isCopied }
}
