import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import { useChatStore } from './stores/chat'
import './assets/styles/theme.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 加载用户信息
const userStore = useUserStore(pinia)
userStore.loadUserInfo()

// 加载好友信息
const chatStore = useChatStore(pinia)
chatStore.loadFriendsFromLocalStorage()

app.mount('#app')
