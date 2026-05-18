<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, type LocationQueryRaw } from 'vue-router'
import DateNav from '@/components/filters/DateNav.vue'
import FiltersBar from '@/components/filters/FiltersBar.vue'
import NewsSection from '@/components/news/NewsSection.vue'
import SingleNewsView from '@/components/news/SingleNewsView.vue'
import DigestModal from '@/components/news/DigestModal.vue'
import LoginForm from '@/components/auth/LoginForm.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseSpinner from '@/components/ui/BaseSpinner.vue'
import { useNewsStore } from '@/stores/news'
import { useFeedsStore } from '@/stores/feeds'
import { useTagGroupsStore } from '@/stores/tagGroups'
import { useFiltersStore } from '@/stores/filters'
import { useUiStore } from '@/stores/ui'
import { useDigestStore } from '@/stores/digest'
import { useModal } from '@/composables/useModal'

const route = useRoute()
const router = useRouter()
const news = useNewsStore()
const feeds = useFeedsStore()
const tagGroups = useTagGroupsStore()
const filters = useFiltersStore()
const ui = useUiStore()
const digest = useDigestStore()

const loading = ref(false)
const loginModal = useModal()
const digestModal = useModal()
const digestLoading = ref(false)

const articleQuery = computed(() => {
  const raw = route.query.article
  if (typeof raw !== 'string') return null
  const id = parseInt(raw, 10)
  return Number.isFinite(id) ? id : null
})

const digestQuery = computed(() => route.query.digest === '1')

const setQuery = (patch: LocationQueryRaw) => {
  router.replace({ query: { ...route.query, ...patch } })
}

const changeDate = async (days: number) => {
  loading.value = true
  await ui.changeDate(days)
  loading.value = false
}

const openDigest = async () => {
  digestModal.open()
  digestLoading.value = true
  if (!digestQuery.value) setQuery({ digest: '1' })
  await digest.fetch()
  digestLoading.value = false
}

const closeDigest = () => {
  digestModal.close()
  setQuery({ digest: undefined })
}

const applyArticleRoute = async (id: number, clearFilters: boolean) => {
  news.deepLinkedId = id
  if (clearFilters) {
    filters.clear()
  }
  await news.fetchById(id)
  ui.singleNewsMode = true
}

onMounted(async () => {
  tagGroups.fetch()
  feeds.fetch()
  news.fetch()

  if (articleQuery.value !== null) {
    await applyArticleRoute(articleQuery.value, true)
  }
  if (digestQuery.value) {
    await openDigest()
  }
})

watch(articleQuery, async (id) => {
  if (id === null) return
  await applyArticleRoute(id, false)
})

watch(digestQuery, async (open) => {
  if (open && !digestModal.isOpen.value) {
    await openDigest()
  } else if (!open && digestModal.isOpen.value) {
    digestModal.close()
  }
})

const singleNewsEntries = computed(() => {
  const entries = news.filtered
  const deep = news.deepLinkedEntry
  if (deep && !entries.some((e) => e.id === deep.id)) {
    return [deep, ...entries]
  }
  return entries
})
</script>

<template>
  <div class="controls-bar">
    <DateNav :loading="loading" @change="changeDate" />
    <FiltersBar @login="loginModal.open()" @digest="openDigest" />
  </div>

  <SingleNewsView
    v-if="ui.singleNewsMode && (news.filtered.length > 0 || news.deepLinkedEntry)"
    :newsEntries="singleNewsEntries"
  />

  <template v-else>
    <div v-if="news.filtered.length > 0">
      <NewsSection :newsEntries="news.nightNews" sectionHeader="Night News" :feedEntries="feeds.entries" />
      <NewsSection :newsEntries="news.afternoonNews" sectionHeader="Afternoon News" :feedEntries="feeds.entries" />
      <NewsSection :newsEntries="news.morningNews" sectionHeader="Morning News" :feedEntries="feeds.entries" />
    </div>
    <p v-else class="empty-state">Keine Nachrichten f&uuml;r diesen Tag</p>
  </template>

  <BaseSpinner v-if="loading" fixed />

  <DigestModal
    v-if="digestModal.isOpen.value"
    :loading="digestLoading"
    @close="closeDigest"
  />

  <BaseModal v-if="loginModal.isOpen.value" @close="loginModal.close()">
    <LoginForm :closeModal="loginModal.close" />
  </BaseModal>
</template>

<style scoped>
.controls-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
}

.empty-state {
  text-align: center;
  color: var(--text-muted);
  font-family: var(--font-ui);
  font-size: 0.85rem;
  padding: 3rem 1rem;
  font-style: italic;
}
</style>
