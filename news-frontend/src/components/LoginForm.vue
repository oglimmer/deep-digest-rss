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
  border: 1px solid #ccc;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login-form form {
  display: flex;
  flex-direction: column;
}

.login-form h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.login-form input {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

/* Fancy error message styling */
.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  margin-bottom: 10px;
  border: 1px solid #cc0000;
  border-radius: 4px;
  background-color: #ffe6e6;
  color: #cc0000;
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
  background-color: #007bff;
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.login-form button:hover {
  background-color: #0056b3;
}

.close-button {
  background-color: #6c757d;
}

.close-button:hover {
  background-color: #5a6268;
}
</style>
