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
  <div class="article">
    <div class="article-header">
      <a :href="entry.url" target="_blank" class="source">[{{ feedTitle }}]</a>
      <span class="ad" v-if="entry.advertising">AD</span>
      <span class="title">{{ entry.title }}</span>
    </div>
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
.article {
  margin-bottom: 1.5em;
  padding-bottom: 1.5em;
  border-bottom: 1px solid var(--border-color);
}

.article:last-child {
  border-bottom: none;
}

.article-header {
  margin-bottom: 0.25em;
}

.source {
  font-size: 0.85em;
  color: var(--text-muted);
  margin-right: 0.5em;
}

.source:visited {
  color: var(--text-muted);
}

.title {
  font-weight: 600;
  color: var(--text-primary);
}

.tags {
  margin-top: 0.5em;
}

.tag {
  display: inline-block;
  border: 1px solid var(--bg-tertiary);
  border-radius: 3px;
  padding: 2px 5px;
  margin: 0 2px;
  background-color: transparent;
  font-size: 0.8em;
  color: var(--text-muted);
}

p {
  padding-left: 1em;
  margin-top: 0.75em;
  margin-bottom: 0;
  hyphens: auto;
  -webkit-hyphens: auto;
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
