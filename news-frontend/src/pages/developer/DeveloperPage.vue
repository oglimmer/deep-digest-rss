<script setup lang="ts">
import { computed, watch, type Component } from 'vue'
import { useRouter } from 'vue-router'
import { CONCEPT_TABS, REFERENCE_TABS, type DeveloperTab } from '@/router'
import CopyButton from './CopyButton.vue'
import { useCopy } from './useCopy'
import { BASE_URL } from './content'
import './developer.css'

import ArchitectureConcept from './concepts/ArchitectureConcept.vue'
import PipelineConcept from './concepts/PipelineConcept.vue'
import AiConcept from './concepts/AiConcept.vue'
import TagsConcept from './concepts/TagsConcept.vue'
import DigestConcept from './concepts/DigestConcept.vue'
import DatabaseConcept from './concepts/DatabaseConcept.vue'
import DeploymentConcept from './concepts/DeploymentConcept.vue'
import ReferenceOverview from './reference/ReferenceOverview.vue'
import ReferenceEndpoints from './reference/ReferenceEndpoints.vue'
import ReferenceDtos from './reference/ReferenceDtos.vue'

type Section = 'concepts' | 'reference'

const props = defineProps<{ concept: DeveloperTab }>()
const router = useRouter()

const { copy, isCopied } = useCopy()

const tabComponents: Record<DeveloperTab, Component> = {
  architecture: ArchitectureConcept,
  pipeline: PipelineConcept,
  ai: AiConcept,
  tags: TagsConcept,
  digest: DigestConcept,
  database: DatabaseConcept,
  deployment: DeploymentConcept,
  overview: ReferenceOverview,
  endpoints: ReferenceEndpoints,
  dtos: ReferenceDtos,
}

const activeTab = computed<DeveloperTab>(() => props.concept)
const activeSection = computed<Section>(() =>
  (REFERENCE_TABS as readonly string[]).includes(activeTab.value) ? 'reference' : 'concepts',
)
const activeComponent = computed(() => tabComponents[activeTab.value])

const conceptTabs: { id: DeveloperTab; label: string; kicker: string }[] = [
  { id: 'architecture', label: 'Architecture', kicker: '01' },
  { id: 'pipeline', label: 'Ingestion', kicker: '02' },
  { id: 'ai', label: 'AI Engines', kicker: '03' },
  { id: 'tags', label: 'Tag Groups', kicker: '04' },
  { id: 'digest', label: 'Daily Digest', kicker: '05' },
  { id: 'database', label: 'Database', kicker: '06' },
  { id: 'deployment', label: 'Deployment', kicker: '07' },
]

const referenceTabs: { id: DeveloperTab; label: string }[] = [
  { id: 'overview', label: 'Overview' },
  { id: 'endpoints', label: 'Endpoints' },
  { id: 'dtos', label: 'DTOs' },
]

const goto = (tab: DeveloperTab) => {
  router.push({ name: 'developer', params: { concept: tab } })
}

const selectSection = (s: Section) => {
  goto(s === 'concepts' ? 'architecture' : 'overview')
}

watch(activeTab, () => {
  window.scrollTo({ top: 0 })
})

void CONCEPT_TABS
</script>

<template>
  <div class="dev-page">
    <div class="dev-header">
      <div class="dev-header-text">
        <h1 class="dev-title">Developer Reference</h1>
        <p class="dev-subtitle">REST API · Authentication · Data Structures</p>
      </div>
      <div class="dev-meta">
        <span class="meta-badge">Base URL</span>
        <code class="meta-url">{{ BASE_URL }}</code>
        <CopyButton :text="BASE_URL" id="base-url" :copied="isCopied('base-url')" @copy="copy" />
      </div>
    </div>

    <div class="nav-shell">
      <div class="section-toggle" role="tablist" aria-label="Documentation section">
        <button
          type="button"
          class="section-btn"
          :class="{ active: activeSection === 'concepts' }"
          role="tab"
          :aria-selected="activeSection === 'concepts'"
          @click="selectSection('concepts')"
        >
          <span class="section-btn-label">Concepts</span>
          <span class="section-btn-count">{{ conceptTabs.length }}</span>
        </button>
        <button
          type="button"
          class="section-btn"
          :class="{ active: activeSection === 'reference' }"
          role="tab"
          :aria-selected="activeSection === 'reference'"
          @click="selectSection('reference')"
        >
          <span class="section-btn-label">API Reference</span>
          <span class="section-btn-count">{{ referenceTabs.length }}</span>
        </button>
      </div>

      <div v-if="activeSection === 'concepts'" class="pill-nav">
        <button
          v-for="t in conceptTabs"
          :key="t.id"
          type="button"
          class="pill"
          :class="{ active: activeTab === t.id }"
          @click="goto(t.id)"
        >
          <span class="pill-kicker">{{ t.kicker }}</span>
          <span class="pill-label">{{ t.label }}</span>
        </button>
      </div>

      <div v-else class="pill-nav">
        <button
          v-for="t in referenceTabs"
          :key="t.id"
          type="button"
          class="pill pill-ref"
          :class="{ active: activeTab === t.id }"
          @click="goto(t.id)"
        >
          <span class="pill-label">{{ t.label }}</span>
        </button>
      </div>
    </div>

    <component :is="activeComponent" />
  </div>
</template>
