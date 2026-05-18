import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const NewsPage = () => import('@/pages/NewsPage.vue')
const DeveloperPage = () => import('@/pages/developer/DeveloperPage.vue')

export const CONCEPT_TABS = [
  'architecture',
  'pipeline',
  'ai',
  'tags',
  'digest',
  'database',
  'deployment',
] as const

export const REFERENCE_TABS = ['overview', 'endpoints', 'dtos'] as const

export type DeveloperTab = (typeof CONCEPT_TABS)[number] | (typeof REFERENCE_TABS)[number]

const VALID_TABS: readonly string[] = [...CONCEPT_TABS, ...REFERENCE_TABS]

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'news',
    component: NewsPage,
  },
  {
    path: '/developer',
    redirect: '/developer/architecture',
  },
  {
    path: '/developer/:concept',
    name: 'developer',
    component: DeveloperPage,
    props: (route) => {
      const concept = String(route.params.concept)
      return {
        concept: VALID_TABS.includes(concept) ? (concept as DeveloperTab) : 'architecture',
      }
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})
