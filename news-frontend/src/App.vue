<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ThemeToggle from './components/ThemeToggle.vue';
import LoginForm from './components/auth/LoginForm.vue';
import { AUTH_EXPIRED_EVENT } from '@/api/client';
import { useAuthStore } from '@/stores/auth';
import { useThemeStore } from '@/stores/theme';

const auth = useAuthStore();
const theme = useThemeStore();
const route = useRoute();
const router = useRouter();
const appsOpen = ref(false);

type AppEntry = {
  name: string;
  url?: string;
  to?: string;
  current?: boolean;
};

const isDeveloper = computed(() => route.path.startsWith('/developer'));

const apps = computed<AppEntry[]>(() => [
  { name: 'Content', url: 'https://content.oglimmer.com/' },
  { name: 'Infographics', url: 'https://infographics.oglimmer.com/' },
  { name: 'News', to: '/', current: !isDeveloper.value },
  { name: 'News - Developer Portal', to: '/developer', current: isDeveloper.value },
  { name: 'Linky', url: 'https://www.linky1.com/' },
]);

const toggleApps = () => {
  appsOpen.value = !appsOpen.value;
};

const navigateTo = (path: string) => {
  router.push(path);
  appsOpen.value = false;
};

const onClickOutside = (e: MouseEvent) => {
  const target = e.target as HTMLElement;
  if (appsOpen.value && !target.closest('.apps-menu')) {
    appsOpen.value = false;
  }
};

const onAuthExpired = () => {
  auth.clear();
};

// LoginForm expects a closeModal callback; the loggedIn flip drives rendering instead.
const noop = () => {};

onMounted(async () => {
  theme.init();
  document.addEventListener('click', onClickOutside);
  window.addEventListener(AUTH_EXPIRED_EVENT, onAuthExpired);
  await auth.hydrate();
});

onUnmounted(() => {
  document.removeEventListener('click', onClickOutside);
  window.removeEventListener(AUTH_EXPIRED_EVENT, onAuthExpired);
});
</script>

<template>
  <!-- While hydrating session, render nothing to avoid a flash of UI. -->
  <main v-if="!auth.hydrated"></main>

  <!-- Logged out: intro + LoginForm. No header, no router-view, no API calls. -->
  <main v-else-if="!auth.loggedIn" class="login-only">
    <div class="auth-card">
      <section class="auth-intro">
        <h1 class="landing-title">Deep Digest RSS</h1>
        <p class="landing-tagline">Readable news, curated &amp; summarized</p>
        <p class="landing-desc">
          Stories from across your RSS feeds, each given a short, neutral AI summary and an
          interest score — so you can skim what matters without opening a dozen tabs.
        </p>
        <ul class="landing-features">
          <li><strong>Aggregated feeds</strong> — many sources collected in one place.</li>
          <li><strong>AI summaries</strong> — every article condensed to the essentials.</li>
          <li><strong>Interest scores</strong> — a quick signal for what's worth your time.</li>
          <li><strong>By time of day</strong> — morning, afternoon and night sections.</li>
        </ul>
      </section>
      <div class="auth-login">
        <LoginForm :closeModal="noop" />
      </div>
    </div>
  </main>

  <!-- Logged in: full app shell. -->
  <main v-else>
    <header class="app-header">
      <div class="header-title-area">
        <h1 class="site-title">Lesbare Nachrichten</h1>
        <span class="site-subtitle">Kuratiert & zusammengefasst</span>
      </div>
      <div class="header-right">
        <div class="apps-menu">
          <button class="apps-btn" @click="toggleApps" title="Switch app">
            <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor">
              <circle cx="3" cy="3" r="1.5" />
              <circle cx="8" cy="3" r="1.5" />
              <circle cx="13" cy="3" r="1.5" />
              <circle cx="3" cy="8" r="1.5" />
              <circle cx="8" cy="8" r="1.5" />
              <circle cx="13" cy="8" r="1.5" />
              <circle cx="3" cy="13" r="1.5" />
              <circle cx="8" cy="13" r="1.5" />
              <circle cx="13" cy="13" r="1.5" />
            </svg>
          </button>
          <div v-if="appsOpen" class="apps-dropdown">
            <template v-for="app in apps" :key="app.name">
              <a
                v-if="app.url"
                :href="app.url"
                class="app-link"
                :class="{ 'app-current': app.current }"
                target="_blank"
                rel="noopener"
              >
                {{ app.name }}
                <span v-if="app.current" class="current-badge">current</span>
              </a>
              <button
                v-else-if="app.to"
                type="button"
                class="app-link"
                :class="{ 'app-current': app.current }"
                @click="navigateTo(app.to)"
              >
                {{ app.name }}
                <span v-if="app.current" class="current-badge">current</span>
              </button>
            </template>
          </div>
        </div>
        <ThemeToggle />
      </div>
    </header>
    <div class="header-rule"></div>
    <RouterView />
  </main>
</template>

<style scoped>
main {
  max-width: 740px;
  margin: 0 auto;
}

.login-only {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 1.5rem;
}

/* Unified card: intro panel + login panel share one border, radius and shadow. */
.auth-card {
  display: flex;
  align-items: stretch;
  width: 100%;
  max-width: 820px;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  box-shadow: 0 12px 40px var(--shadow-strong), 0 2px 8px var(--shadow-color);
  overflow: hidden;
}

.auth-intro {
  flex: 1 1 0;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 2.5rem;
  background-color: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
}

.auth-login {
  display: flex;
  align-items: center;
  justify-content: center;
}

.landing-title {
  font-family: var(--font-display);
  font-size: 1.85rem;
  font-weight: 400;
  margin: 0;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.landing-tagline {
  font-family: var(--font-ui);
  font-size: 0.68rem;
  font-weight: 600;
  color: var(--primary-color);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin: 0.6rem 0 1.5rem 0;
}

.landing-desc {
  font-family: var(--font-display);
  font-size: 1rem;
  line-height: 1.6;
  color: var(--text-secondary);
  margin: 0 0 1.5rem 0;
}

.landing-features {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.landing-features li {
  font-family: var(--font-ui);
  font-size: 0.85rem;
  line-height: 1.5;
  color: var(--text-secondary);
  padding-left: 1.1rem;
  position: relative;
}

.landing-features li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0.5em;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background-color: var(--primary-color);
}

.landing-features strong {
  color: var(--text-primary);
  font-weight: 600;
}

/* Stack into a single column on narrow viewports. */
@media (max-width: 720px) {
  .auth-card {
    flex-direction: column;
    max-width: 400px;
  }

  .auth-intro {
    min-width: 0;
    padding: 2rem 2rem 1.75rem;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }
}

.app-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.header-title-area {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
}

.site-title {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 400;
  margin: 0;
  color: var(--text-primary);
  white-space: nowrap;
  letter-spacing: -0.02em;
  line-height: 1;
}

.site-subtitle {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  color: var(--text-muted);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
}

.header-rule {
  height: 2px;
  background: linear-gradient(90deg, var(--accent-line), var(--accent-line) 30%, var(--border-color) 30%, var(--border-color));
  margin-bottom: 1.25rem;
  border-radius: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.apps-menu {
  position: relative;
}

.apps-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.35rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.apps-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.apps-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 150px;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: 0 8px 24px var(--shadow-strong), 0 2px 8px var(--shadow-color);
  z-index: 100;
  padding: 0.375rem;
  animation: dropdownReveal 0.15s ease;
}

@keyframes dropdownReveal {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.app-link {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0.625rem;
  color: var(--text-primary);
  text-decoration: none;
  font-family: var(--font-ui);
  font-size: 0.78rem;
  font-weight: 500;
  border: none;
  border-radius: 5px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.15s;
}

.app-link:hover {
  background-color: var(--bg-hover);
  text-decoration: none;
}

.app-current {
  color: var(--text-muted);
}

.current-badge {
  font-size: 0.575rem;
  color: var(--primary-color);
  margin-left: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 600;
}

@media (max-width: 480px) {
  .site-title {
    font-size: 1.2rem;
  }

  .site-subtitle {
    display: none;
  }

  .header-title-area {
    gap: 0.5rem;
  }
}
</style>
