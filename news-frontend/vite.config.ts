import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  define: {
    __API_URL__: JSON.stringify(process.env.API_URL ?? "http://localhost:8080"),
    __API_USER__: JSON.stringify(process.env.API_USER ?? "read"),
    __API_PASSWORD__: JSON.stringify(process.env.API_PASSWORD ?? "read")
  },
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
