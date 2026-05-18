import { ref } from 'vue'

export const useModal = (initial = false) => {
  const isOpen = ref(initial)

  return {
    isOpen,
    open: () => {
      isOpen.value = true
    },
    close: () => {
      isOpen.value = false
    },
    toggle: () => {
      isOpen.value = !isOpen.value
    },
  }
}
