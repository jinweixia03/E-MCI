import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

// 用户类型: 0-普通用户, 1-管理员, 2-维修员
export type UserType = 0 | 1 | 2

// 定义哪些角色可以访问该路由（不设置则所有登录用户可访问）
export interface RouteMeta {
  public?: boolean
  title?: string
  icon?: string
  hidden?: boolean
  roles?: UserType[]
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true } as RouteMeta
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { public: true } as RouteMeta
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/forgot-password/index.vue'),
    meta: { public: true } as RouteMeta
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' } as RouteMeta
      },
      {
        path: 'map',
        name: 'Map',
        component: () => import('@/views/map/index.vue'),
        meta: { title: '井盖地图', icon: 'MapLocation' } as RouteMeta
      },
      {
        path: 'detection',
        name: 'Detection',
        component: () => import('@/views/detection/index.vue'),
        meta: { title: 'AI检测', icon: 'VideoCamera', roles: [0, 1] } as RouteMeta
      },
      {
        path: 'manhole',
        name: 'Manhole',
        component: () => import('@/views/manhole/index.vue'),
        meta: { title: '井盖管理', icon: 'Grid', roles: [0, 1] } as RouteMeta
      },
      {
        path: 'drone',
        name: 'Drone',
        component: () => import('@/views/drone/index.vue'),
        meta: { title: '无人机管理', icon: 'Aim', roles: [0, 1] } as RouteMeta
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/repair/index.vue'),
        meta: { title: '维修调度', icon: 'Tools' } as RouteMeta
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据大屏', icon: 'DataLine', roles: [0, 1] } as RouteMeta
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '用户中心', icon: 'UserFilled' } as RouteMeta
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()

  const userStore = useUserStore()

  if (to.meta.public) {
    next()
  } else if (!userStore.isLoggedIn) {
    next('/login')
  } else {
    // 检查角色权限
    const allowedRoles = to.meta.roles as number[] | undefined
    const userType = userStore.userInfo?.type ?? 0

    if (allowedRoles && !allowedRoles.includes(userType)) {
      // 没有权限，重定向到首页
      ElMessage.warning('您没有权限访问该页面')
      next('/home')
    } else {
      next()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
