<script setup lang="ts">
import { computed, ref } from 'vue'
import { onClickOutside } from '@vueuse/core'
import MultiSelectDropdown from '@/components/ui/MultiSelectDropdown.vue'
import { useAuthStore } from '@/stores/auth'
import { useFeedsStore } from '@/stores/feeds'
import { useNewsStore } from '@/stores/news'
import { useTagGroupsStore } from '@/stores/tagGroups'
import { useFiltersStore } from '@/stores/filters'

const emit = defineEmits<{ login: []; digest: [] }>()

const auth = useAuthStore()
const feeds = useFeedsStore()
const news = useNewsStore()
const tagGroups = useTagGroupsStore()
const filters = useFiltersStore()

const feedDropdownOpen = ref(false)
const includeDropdownOpen = ref(false)
const excludeDropdownOpen = ref(false)

const closeAll = () => {
  feedDropdownOpen.value = false
  includeDropdownOpen.value = false
  excludeDropdownOpen.value = false
}

const root = ref<HTMLElement | null>(null)
onClickOutside(root, closeAll)

const feedOptions = computed(() =>
  feeds.entries.map((feed) => ({
    value: feed.id,
    label: feed.title,
    count: news.entries.filter((entry) => entry.feedId === feed.id).length,
  })),
)

const tagGroupOptions = computed(() =>
  tagGroups.keys.map((key) => ({
    value: key,
    label: key,
    count: tagGroups.counts[key] ?? 0,
  })),
)
</script>

<template>
  <div class="filters-bar" ref="root">
    <button @click="emit('digest')" class="control-btn digest-btn" title="Daily Digest">
      Digest
    </button>

    <button v-if="!auth.loggedIn" @click="emit('login')" class="control-btn" title="Login">
      Login
    </button>

    <MultiSelectDropdown
      label="Feeds"
      :options="feedOptions"
      :open="feedDropdownOpen"
      v-model="filters.selectedFeeds"
      @update:open="feedDropdownOpen = $event"
    />

    <MultiSelectDropdown
      label="Include"
      :options="tagGroupOptions"
      :open="includeDropdownOpen"
      v-model="filters.selectedTagGroups"
      @update:open="includeDropdownOpen = $event"
    />

    <MultiSelectDropdown
      label="Exclude"
      empty-label="None"
      :options="tagGroupOptions"
      :open="excludeDropdownOpen"
      v-model="filters.excludedTagGroups"
      @update:open="excludeDropdownOpen = $event"
    />
  </div>
</template>

<style scoped>
.filters-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.control-btn {
  padding: 0.35rem 0.75rem;
  border: 1px solid var(--primary-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--primary-color);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.03em;
  transition: all 0.2s ease;
}

.control-btn:hover {
  background-color: var(--primary-color);
  color: #fff;
}
</style>
