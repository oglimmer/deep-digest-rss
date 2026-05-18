<script setup lang="ts">
import { ref } from 'vue'
import CopyButton from '../CopyButton.vue'
import { endpoints } from '../content'
import { useCopy } from '../useCopy'

const { copy, isCopied } = useCopy()

const expanded = ref<string | null>(null)
const toggle = (id: string) => {
  expanded.value = expanded.value === id ? null : id
}
</script>

<template>
  <div class="tab-content">
    <div v-for="group in endpoints" :key="group.group" class="endpoint-group">
      <h2 class="group-title">{{ group.group }}</h2>
      <div class="endpoint-list">
        <div
          v-for="ep in group.items"
          :key="ep.id"
          class="endpoint-card"
          :class="{ 'endpoint-expanded': expanded === ep.id }"
        >
          <button class="endpoint-summary" @click="toggle(ep.id)">
            <span :class="['method-badge', `method-${ep.method.toLowerCase()}`]">{{ ep.method }}</span>
            <code class="ep-path">{{ ep.path }}</code>
            <span :class="['role-badge', `role-${ep.role.toLowerCase()}`]">{{ ep.role }}</span>
            <span class="ep-summary-text">{{ ep.summary }}</span>
            <span class="ep-chevron">{{ expanded === ep.id ? '▲' : '▼' }}</span>
          </button>

          <div v-if="expanded === ep.id" class="endpoint-detail">
            <div v-if="ep.params.length > 0" class="detail-section">
              <div class="detail-label">Parameters</div>
              <table class="param-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Type / Location</th>
                    <th>Required</th>
                    <th>Description</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in ep.params" :key="p.name">
                    <td><code>{{ p.name }}</code></td>
                    <td>{{ p.type }}</td>
                    <td>{{ p.required ? 'yes' : 'no' }}</td>
                    <td>{{ p.desc }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="ep.request" class="detail-section">
              <div class="detail-label">Request Body</div>
              <div class="code-block">
                <pre>{{ ep.request }}</pre>
                <CopyButton :text="ep.request" :id="`${ep.id}-req`" :copied="isCopied(`${ep.id}-req`)" @copy="copy" />
              </div>
            </div>

            <div v-if="ep.response" class="detail-section">
              <div class="detail-label">Response</div>
              <div class="code-block">
                <pre>{{ ep.response }}</pre>
                <CopyButton :text="ep.response" :id="`${ep.id}-res`" :copied="isCopied(`${ep.id}-res`)" @copy="copy" />
              </div>
            </div>

            <div v-if="ep.notes" class="detail-section">
              <div class="detail-note">{{ ep.notes }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
