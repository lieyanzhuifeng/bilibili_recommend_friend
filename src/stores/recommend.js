import { defineStore } from 'pinia'
import { recommendApi, filterApi } from '../services/api'

export const useRecommendStore = defineStore('recommend', {
  state: () => ({
    coComment: [],
    reply: [],
    sharedVideo: [],
    category: [],
    theme: [],
    userBehavior: [],
    commonUp: [],
    favoriteSimilarity: [],
    filters: {},
    loading: false,
    error: null
  }),

  actions: {
    async fetchCoComment(userId) {
      this.loading = true
      this.error = null
      try {
        this.coComment = await recommendApi.coComment(userId)
      } catch (e) {
        console.error('fetchCoComment error', e)
        this.coComment = []
        this.error = e.message || String(e)
      } finally {
        this.loading = false
      }
    },

    async fetchReply(userId) {
      this.loading = true
      this.error = null
      try {
        this.reply = await recommendApi.reply(userId)
      } catch (e) {
        console.error('fetchReply error', e)
        this.reply = []
        this.error = e.message || String(e)
      } finally {
        this.loading = false
      }
    },

    async fetchSharedVideo(userId) {
      this.loading = true
      this.error = null
      try {
        this.sharedVideo = await recommendApi.sharedVideo(userId)
      } catch (e) {
        console.error('fetchSharedVideo error', e)
        this.sharedVideo = []
        this.error = e.message || String(e)
      } finally {
        this.loading = false
      }
    },

    async fetchCategory(userId) {
      this.loading = true
      this.error = null
      try {
        this.category = await recommendApi.category(userId)
      } catch (e) {
        console.error('fetchCategory error', e)
        this.category = []
        this.error = e.message || String(e)
      } finally {
        this.loading = false
      }
    },

    async fetchTheme(userId) {
      this.loading = true
      this.error = null
      try {
        this.theme = await recommendApi.theme(userId)
      } catch (e) {
        console.error('fetchTheme error', e)
        this.theme = []
        this.error = e.message || String(e)
      } finally {
        this.loading = false
      }
    },

    async fetchUserBehavior(userId) {
      this.loading = true
      this.error = null
      try {
        this.userBehavior = await recommendApi.userBehavior(userId)
      } catch (e) {
        console.error('fetchUserBehavior error', e)
        this.userBehavior = []
        this.error = e.message || String(e)
      } finally {
        this.loading = false
      }
    },

    async fetchCommonUp(userId) {
      this.loading = true
      this.error = null
      try {
        this.commonUp = await recommendApi.commonUp(userId)
      } catch (e) {
        console.error('fetchCommonUp error', e)
        this.commonUp = []
        this.error = e.message || String(e)
      } finally {
        this.loading = false
      }
    },

    async fetchFavoriteSimilarity(userId) {
      this.loading = true
      this.error = null
      try {
        this.favoriteSimilarity = await recommendApi.favoriteSimilarity(userId)
      } catch (e) {
        console.error('fetchFavoriteSimilarity error', e)
        this.favoriteSimilarity = []
        this.error = e.message || String(e)
      } finally {
        this.loading = false
      }
    },

    // 示例筛选接口
    async fetchSameUp({ upId, durationOption } = {}) {
      this.loading = true
      this.error = null
      try {
        const data = await filterApi.sameUp({ upId, durationOption })
        this.filters.sameUp = data
        return data
      } catch (e) {
        console.error('fetchSameUp error', e)
        this.filters.sameUp = []
        this.error = e.message || String(e)
        return []
      } finally {
        this.loading = false
      }
    },

    async fetchSameTag({ tagId, durationOption } = {}) {
      this.loading = true
      this.error = null
      try {
        const data = await filterApi.sameTag({ tagId, durationOption })
        this.filters.sameTag = data
        return data
      } catch (e) {
        console.error('fetchSameTag error', e)
        this.filters.sameTag = []
        this.error = e.message || String(e)
        return []
      } finally {
        this.loading = false
      }
    },

    async postSameUpVideoCount(body) {
      this.loading = true
      this.error = null
      try {
        const data = await filterApi.sameUpVideoCount(body)
        this.filters.sameUpVideoCount = data
        return data
      } catch (e) {
        console.error('postSameUpVideoCount error', e)
        this.filters.sameUpVideoCount = []
        this.error = e.message || String(e)
        return []
      } finally {
        this.loading = false
      }
    }
  }
})
