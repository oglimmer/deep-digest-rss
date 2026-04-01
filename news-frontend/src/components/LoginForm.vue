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
    <div class="login-header">
      <h3>Login</h3>
      <span class="login-brand">DeepDigestRSS</span>
    </div>
    <form @submit.prevent="loginHandler">
      <div class="input-group">
        <label for="login-email">Email</label>
        <input id="login-email" type="email" v-model="email" placeholder="you@example.com" required />
      </div>
      <div class="input-group">
        <label for="login-password">Password</label>
        <input id="login-password" type="password" v-model="password" placeholder="Enter password" required />
      </div>
      <!-- Display error message if any -->
      <div v-if="error" class="error-message">{{ error }}</div>
      <div class="button-group">
        <button type="submit" class="btn-primary">Login</button>
        <button type="button" @click="props.closeModal" class="btn-secondary">Cancel</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.login-form {
  width: 360px;
  max-width: 90vw;
  padding: 2rem;
}

.login-header {
  margin-bottom: 1.5rem;
}

.login-header h3 {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 400;
  margin: 0 0 0.25rem 0;
  color: var(--text-primary);
}

.login-brand {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.login-form form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.input-group label {
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.login-form input {
  padding: 0.65rem 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 0.9rem;
  font-family: var(--font-ui);
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  transition: all 0.2s ease;
}

.login-form input::placeholder {
  color: var(--text-muted);
  font-weight: 400;
}

.login-form input:focus {
  outline: none;
  border-color: var(--primary-color);
  background-color: var(--bg-primary);
  box-shadow: 0 0 0 3px rgba(184, 76, 46, 0.08);
}

[data-theme="dark"] .login-form input:focus {
  box-shadow: 0 0 0 3px rgba(224, 122, 82, 0.12);
}

/* Error message styling */
.error-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 0.75rem;
  border: 1px solid var(--error-border);
  border-radius: 6px;
  background-color: var(--error-bg);
  color: var(--error-color);
  font-family: var(--font-ui);
  font-weight: 500;
  font-size: 0.8rem;
}
.error-message::before {
  content: "!";
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 50%;
  background-color: var(--error-color);
  color: #fff;
  font-size: 0.65rem;
  font-weight: 700;
  flex-shrink: 0;
}

.button-group {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  padding: 0.65rem;
  border: none;
  border-radius: 6px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 0.02em;
}

.btn-primary {
  background-color: var(--primary-color);
  color: #fff;
}

.btn-primary:hover {
  background-color: var(--primary-hover);
}

.btn-secondary {
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
}
</style>
