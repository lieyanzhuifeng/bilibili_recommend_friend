import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false, title: '登录 - Bfriend' }
    },
    {
      path: '/',
      component: () => import('../layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/Profile.vue'),
          meta: { title: '个人资料 - Bfriend' }
        },
        {
          path: 'friends',
          name: 'friends',
          component: () => import('../views/Friends.vue'),
          meta: { title: '好友列表 - Bfriend' }
        },
        {
          path: 'user-info',
          name: 'userInfo',
          component: () => import('../views/UserInfo.vue'),
          meta: { title: '用户信息 - Bfriend' }
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('../views/Chat.vue')
          // 聊天页面不设置固定标题，将在组件内根据聊天对象动态设置
        }
        ,
        {
          path: 'recommendations',
          name: 'recommendations',
          component: () => import('../views/Recommendations.vue'),
          meta: { title: '推荐用户 - Bfriend' }
        },
        {
          path: 'friend-management',
          name: 'friendManagement',
          component: () => import('../views/FriendManagement.vue'),
          meta: { title: '好友管理 - Bfriend' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  } else {
    document.title = 'Bfriend'
  }

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
