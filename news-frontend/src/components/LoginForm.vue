<script setup lang="ts">
import { ref } from 'vue';
import { useDataStore } from '@/stores/data';

const props = defineProps<{ closeModal: () => void }>();

const dataStore = useDataStore();

const email = ref('');
const password = ref('');
const error = ref('');

const loginHandler = async () => {
  const result = await dataStore.authorize(email.value, password.value);
  if (result) {
    error.value = result;
  } else {
    props.closeModal();
  }
};
</script>

<template>
  <div class="login-form">
    <h3>Login @ DeepDigestRSS</h3>
    <form @submit.prevent="loginHandler">
      <input type="email" v-model="email" placeholder="Email" required />
      <input type="password" v-model="password" placeholder="Password" required />
      <!-- Display error message if any -->
      <div v-if="error" class="error-message">{{ error }}</div>
      <div class="button-group">
        <button type="submit">Login</button>
        <button type="button" @click="props.closeModal" class="close-button">Close</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.login-form {
  max-width: 400px;
  padding: 20px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: var(--bg-primary);
  box-shadow: 0 2px 10px var(--shadow-color);
}

.login-form form {
  display: flex;
  flex-direction: column;
}

.login-form h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: var(--text-primary);
}

.login-form input {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
  background-color: var(--bg-primary);
  color: var(--text-primary);
}

.login-form input::placeholder {
  color: var(--text-muted);
}

.login-form input:focus {
  outline: none;
  border-color: var(--primary-color);
}

/* Fancy error message styling */
.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  margin-bottom: 10px;
  border: 1px solid var(--error-border);
  border-radius: 4px;
  background-color: var(--error-bg);
  color: var(--error-color);
  font-weight: bold;
  font-size: 0.9rem;
}
.error-message::before {
  content: "⚠";
  font-size: 1.2rem;
}

.button-group {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.login-form button {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 4px;
  background-color: var(--primary-color);
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.login-form button:hover {
  background-color: var(--primary-hover);
}

.close-button {
  background-color: var(--secondary-color);
}

.close-button:hover {
  background-color: var(--secondary-hover);
}
</style>
