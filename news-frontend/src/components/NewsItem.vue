<script setup lang="ts">
import { useDataStore } from '@/stores/data';

const dataStore = useDataStore();

const props = defineProps(['entry', 'feedTitle'])

const sendVoteUp = async () => {
  dataStore.addVote(props.entry.id, true);
};
const sendVoteDown = async () => {
  dataStore.addVote(props.entry.id, false);
};
</script>

<template>
  <div>
    <a :href="entry.url" target="_blank">[{{ feedTitle }}]</a>
    <span class="ad" v-if="entry.advertising">AD</span>
    {{ entry.title }}
    <div class="tags">
      <span class="tag" v-for="tag in entry.tags" :key="tag">{{ tag }}</span>
      <span v-if="dataStore.loggedIn && !entry.voted"> &nbsp;
        <a href="#" @click.prevent="sendVoteUp">+1</a>
      </span>
      <span v-if="dataStore.loggedIn && entry.voted"> &nbsp;
        <a href="#" @click.prevent="sendVoteDown">-1</a>
      </span>
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
  border: 1px solid var(--border-color);
  border-radius: 3px;
  padding: 2px 5px;
  margin: 0 2px;
  background-color: var(--bg-tertiary);
  font-size: 0.9em;
  color: var(--text-primary);
}

.ad {
  display: inline-block;
  border: 1px solid var(--ad-border);
  border-radius: 3px;
  padding: 2px 5px;
  margin: 0 2px;
  background-color: var(--ad-bg);
  font-size: 0.9em;
  color: var(--ad-color);
  font-weight: bold;
}
</style>
