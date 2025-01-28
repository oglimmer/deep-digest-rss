<script setup lang="ts">
import NewsItem from './NewsItem.vue';

defineProps(["newsEntries", "sectionHeader"])

</script>

<template>
  
  <h3 v-if="newsEntries.length > 0">{{sectionHeader}}</h3>
  <ul v-if="newsEntries.length > 0">
    <li v-for="entry in newsEntries" :key="entry.id">
      <NewsItem :entry="entry" />
    </li>
  </ul>
</template>

<style scoped>

</style>
