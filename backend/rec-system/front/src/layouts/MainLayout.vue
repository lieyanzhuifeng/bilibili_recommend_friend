<template>
  <div class="main-layout">
    <Navbar />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script>
import Navbar from '../components/Navbar.vue'

export default {
  name: 'MainLayout',
  components: {
    Navbar
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  margin-top: 60px; /* 与导航栏高度一致 */
  padding: 20px;
  flex: 1;
}
</style>