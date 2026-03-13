import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginResponse } from '@/types/user'

export const useUserStore = defineStore(
  'user',
  () => {
    // State
    const token = ref<string>('')
    const userInfo = ref<UserInfo | null>(null)

    // Getters
    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => userInfo.value?.type === 1)
    const isRepairer = computed(() => userInfo.value?.type === 2)

    // Actions
    const setToken = (newToken: string) => {
      token.value = newToken
    }

    const setUserInfo = (info: UserInfo) => {
      userInfo.value = info
    }

    const login = (data: LoginResponse) => {
      token.value = data.accessToken
      userInfo.value = data.userInfo
    }

    const logout = () => {
      token.value = ''
      userInfo.value = null
    }

    return {
      token,
      userInfo,
      isLoggedIn,
      isAdmin,
      isRepairer,
      setToken,
      setUserInfo,
      login,
      logout
    }
  },
  {
    persist: true
  }
)
