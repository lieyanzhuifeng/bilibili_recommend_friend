<template>
  <div>
    Test Component
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRecommendStore } from '@/store/recommend'

const recommendStore = useRecommendStore()
const activeFilter = ref('commonUp')

async function applyFilter() {
  try {
    let results = []
    
    switch (activeFilter.value) {
      case 'commonUp':
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 100))
        results = recommendStore.commonUp || []
        break
      case 'sharedVideo':
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 100))
        results = recommendStore.sharedVideo || []
        break
      default:
        results = []
    }
    
    return results
  } catch (error) {
    console.error('Error applying filter:', error)
    return []
  }
}

function createMockResults() {
  return [
    { id: 1, username: 'Test User', similarityRate: 0.95 }
  ]
}

// 返回需要暴露的属性
defineExpose({
  applyFilter,
  createMockResults
})
</script>

<style scoped>
</style>