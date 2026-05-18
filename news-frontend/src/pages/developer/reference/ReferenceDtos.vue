<script setup lang="ts">
import { dtos } from '../content'
</script>

<template>
  <div class="tab-content">
    <p class="dtos-intro">All DTOs use JSON. Java types map as: <code>Long → number</code>, <code>Instant → ISO-8601 string</code>, <code>boolean → boolean</code>, <code>String[] → string[]</code>.</p>
    <div class="dto-grid">
      <div v-for="dto in dtos" :key="dto.name" class="dto-card">
        <div class="dto-header">
          <span class="dto-name">{{ dto.name }}</span>
          <span :class="['dto-dir', dto.direction === 'Request' ? 'dto-req' : 'dto-res']">
            {{ dto.direction }}
          </span>
        </div>
        <p class="dto-desc">{{ dto.description }}</p>
        <table class="dto-table">
          <thead>
            <tr>
              <th>Field</th>
              <th>Type</th>
              <th>Notes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in dto.fields" :key="f.name">
              <td><code>{{ f.name }}</code></td>
              <td><code class="type-code">{{ f.type }}</code></td>
              <td class="notes-cell">{{ f.notes }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
