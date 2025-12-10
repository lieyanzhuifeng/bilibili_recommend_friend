import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('../layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/Profile.vue')
        },
        {
          path: 'friends',
          name: 'friends',
          component: () => import('../views/Friends.vue')
        },
        {
          path: 'user-info',
          name: 'userInfo',
          component: () => import('../views/UserInfo.vue')
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('../views/Chat.vue')
        }
        ,
        {
          path: 'recommendations',
          name: 'recommendations',
          component: () => import('../views/Recommendations.vue')
        },
        {
          path: 'friend-management',
          name: 'friendManagement',
          component: () => import('../views/FriendManagement.vue')
        },
        {
          path: 'filter',
          name: 'filter',
          component: () => import('../views/Filter.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 获取用户store
  const userStore = useUserStore()

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    // 如果用户未登录，尝试从localStorage加载用户信息
    if (!userStore.isLoggedIn) {
      userStore.loadUserInfo()
    }

    // 检查是否已登录
    if (!userStore.isLoggedIn) {
      // 没有登录信息，重定向到登录页
      next('/')
    } else {
      next()
    }
  } else {
    // 如果是访问登录页，但用户已登录，则重定向到个人主页
    if (to.path === '/' && userStore.isLoggedIn) {
      next('/profile')
    } else {
      next()
    }
  }
})

export default router
