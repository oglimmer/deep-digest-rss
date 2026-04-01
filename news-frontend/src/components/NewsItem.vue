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
      <a :href="entry.url" target="_blank" class="source">{{ feedTitle }}</a>
      <span class="ad" v-if="entry.advertising">AD</span>
    </div>
    <h4 class="title">{{ entry.title }}</h4>
    <p>{{ entry.text }}</p>
    <div class="article-footer">
      <div class="tags">
        <span class="tag" v-for="tag in entry.tags" :key="tag">{{ tag }}</span>
      </div>
      <div class="vote-actions" v-if="dataStore.loggedIn">
        <a v-if="!entry.voted" href="#" @click.prevent="sendVoteUp" class="vote-btn vote-up" title="Upvote">+1</a>
        <a v-if="entry.voted" href="#" @click.prevent="sendVoteDown" class="vote-btn vote-down" title="Remove vote">-1</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.article {
  margin-bottom: 0;
  padding: 1.25rem 0;
  border-bottom: 1px solid var(--border-color);
  position: relative;
  transition: background-color 0.2s ease;
}

.article:last-child {
  border-bottom: none;
}

.article-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.375rem;
}

.source {
  font-family: var(--font-ui);
  font-size: 0.68rem;
  font-weight: 600;
  color: var(--primary-color);
  letter-spacing: 0.04em;
  text-transform: uppercase;
  text-decoration: none;
}

.source:visited {
  color: var(--primary-color);
}

.source:hover {
  text-decoration: underline;
  text-underline-offset: 2px;
}

.title {
  font-family: var(--font-display);
  font-weight: 400;
  font-size: calc(var(--font-size-base) * 1.1);
  color: var(--text-primary);
  margin: 0 0 0.25rem 0;
  line-height: 1.35;
  letter-spacing: -0.01em;
}

p {
  margin-top: 0.5rem;
  margin-bottom: 0;
  hyphens: auto;
  -webkit-hyphens: auto;
  color: var(--text-secondary);
  line-height: 1.7;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.75rem;
  gap: 0.5rem;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.tag {
  display: inline-block;
  border: 1px solid var(--border-color);
  border-radius: 3px;
  padding: 0.125rem 0.4rem;
  background-color: transparent;
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 500;
  color: var(--text-muted);
  letter-spacing: 0.02em;
  transition: all 0.15s ease;
}

.tag:hover {
  border-color: var(--border-hover);
  color: var(--text-secondary);
}

.vote-actions {
  flex-shrink: 0;
}

.vote-btn {
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  text-decoration: none;
  transition: all 0.15s ease;
}

.vote-up {
  color: var(--primary-color);
}

.vote-up:hover {
  background-color: var(--primary-color);
  color: #fff;
  text-decoration: none;
}

.vote-down {
  color: var(--text-muted);
}

.vote-down:hover {
  color: var(--text-primary);
  text-decoration: none;
}

.ad {
  display: inline-block;
  border: 1px solid var(--ad-border);
  border-radius: 3px;
  padding: 0.1rem 0.35rem;
  background-color: var(--ad-bg);
  font-family: var(--font-ui);
  font-size: 0.6rem;
  font-weight: 700;
  color: var(--ad-color);
  letter-spacing: 0.06em;
}
</style>
