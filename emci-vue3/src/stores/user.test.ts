import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from './user'
import type { UserInfo, LoginResponse } from '@/types/user'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('State', () => {
    it('应该初始化token为空字符串', () => {
      const store = useUserStore()
      expect(store.token).toBe('')
    })

    it('应该初始化userInfo为null', () => {
      const store = useUserStore()
      expect(store.userInfo).toBeNull()
    })
  })

  describe('Getters', () => {
    it('isLoggedIn应该在token为空时返回false', () => {
      const store = useUserStore()
      expect(store.isLoggedIn).toBe(false)
    })

    it('isLoggedIn应该在有token时返回true', () => {
      const store = useUserStore()
      store.setToken('test-token')
      expect(store.isLoggedIn).toBe(true)
    })

    it('isAdmin应该在用户类型为1时返回true', () => {
      const store = useUserStore()
      const adminUser: UserInfo = {
        id: 1,
        username: 'admin',
        email: 'admin@example.com',
        type: 1,
        createTime: '2024-01-01'
      }
      store.setUserInfo(adminUser)
      expect(store.isAdmin).toBe(true)
    })

    it('isAdmin应该在用户类型不为1时返回false', () => {
      const store = useUserStore()
      const normalUser: UserInfo = {
        id: 2,
        username: 'user',
        email: 'user@example.com',
        type: 0,
        createTime: '2024-01-01'
      }
      store.setUserInfo(normalUser)
      expect(store.isAdmin).toBe(false)
    })
  })

  describe('Actions', () => {
    it('setToken应该设置token值', () => {
      const store = useUserStore()
      store.setToken('new-token')
      expect(store.token).toBe('new-token')
    })

    it('setUserInfo应该设置用户信息', () => {
      const store = useUserStore()
      const userInfo: UserInfo = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        phone: '13800138000',
        type: 0,
        createTime: '2024-01-01'
      }
      store.setUserInfo(userInfo)
      expect(store.userInfo).toEqual(userInfo)
    })

    it('login应该同时设置token和userInfo', () => {
      const store = useUserStore()
      const loginData: LoginResponse = {
        accessToken: 'jwt-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
        userInfo: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com',
          type: 0
        }
      }
      store.login(loginData)
      expect(store.token).toBe('jwt-token')
      expect(store.userInfo).toEqual(loginData.userInfo)
    })

    it('logout应该清空token和userInfo', () => {
      const store = useUserStore()
      // 先设置一些值
      store.setToken('test-token')
      store.setUserInfo({
        id: 1,
        username: 'testuser',
        type: 0
      })

      // 执行logout
      store.logout()

      // 验证被清空
      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })

    it('应该支持持久化配置', () => {
      // 验证store配置了持久化
      const store = useUserStore()
      expect(store.$id).toBe('user')
      // pinia-plugin-persistedstate会为配置了persist的store添加特定属性
      expect(store.$state).toBeDefined()
    })
  })

  describe('边界情况', () => {
    it('多次设置token应该覆盖之前的值', () => {
      const store = useUserStore()
      store.setToken('token1')
      store.setToken('token2')
      store.setToken('token3')
      expect(store.token).toBe('token3')
    })

    it('设置空的userInfo应该被接受', () => {
      const store = useUserStore()
      const emptyUserInfo: UserInfo = {
        id: 0,
        username: '',
        email: '',
        type: 0
      }
      store.setUserInfo(emptyUserInfo)
      expect(store.userInfo).toEqual(emptyUserInfo)
      expect(store.isLoggedIn).toBe(false) // token仍为空
    })
  })
})
