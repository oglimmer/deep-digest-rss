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
