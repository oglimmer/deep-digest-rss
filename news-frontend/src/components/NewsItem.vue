<script setup lang="ts">

defineProps(["entry"])

</script>

<template>
    <div>
        <a :href="entry.url" target="_blank">[{{ entry.feedId }}]</a> 
        <span class="ad" v-if="entry.advertising">AD</span> 
        {{ entry.title }}
        <div class="tags">
          <span class="tag" v-for="tag in entry.tags">{{ tag }}</span>
        </div>
        <p>{{ entry.text }}</p>
    </div>
</template>

<style scoped>
.tags {
  margin-top: 9px;
}
.tag {
  display: inline-block;
  border: 1px solid #ccc;
  border-radius: 3px;
  padding: 2px 5px;
  margin: 0 2px;
  background-color: #f0f0f0;
  font-size: 0.9em;
  color: #333;
}
.ad {
  display: inline-block;
  border: 1px solid #ff0000;
  border-radius: 3px;
  padding: 2px 5px;
  margin: 0 2px;
  background-color: #ffcccc;
  font-size: 0.9em;
  color: #ff0000;
  font-weight: bold;
}

</style>
