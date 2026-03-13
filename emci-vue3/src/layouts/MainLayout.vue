<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside
      :width="isCollapse ? '120px' : '260px'"
      class="sidebar"
      :class="{ 'sidebar-collapsed': isCollapse }"
    >
      <!-- Logo区域 -->
      <div class="logo" @click="router.push('/')">
        <div class="logo-icon">
          <el-icon :size="28"><Monitor /></el-icon>
        </div>
        <transition name="fade">
          <div v-show="!isCollapse" class="logo-text">
            <h2>EMCI</h2>
            <p>智能井盖检测系统</p>
          </div>
        </transition>
      </div>

      <!-- 菜单 -->
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <el-menu-item
          v-for="route in menuRoutes"
          :key="route.path"
          :index="route.path"
          class="menu-item"
        >
          <el-icon class="menu-icon">
            <component :is="route.meta?.icon" />
          </el-icon>
          <template #title>
            <span class="menu-title">{{ route.meta?.title }}</span>
          </template>
        </el-menu-item>
      </el-menu>

      <!-- 侧边栏底部 -->
      <div class="sidebar-footer">
        <el-tooltip
          :content="isCollapse ? '展开菜单' : '收起菜单'"
          placement="right"
        >
          <div class="collapse-btn" @click="toggleSidebar">
            <el-icon :size="18" :class="{ 'rotate-180': isCollapse }">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
        </el-tooltip>
      </div>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container class="main-wrapper">
      <!-- 头部 -->
      <el-header class="header" :class="{ 'header-scrolled': isScrolled }">
        <div class="header-left">
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">
              <el-icon><HomeFilled /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta?.title">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 搜索框 -->
          <div class="header-search">
            <el-input
              v-model="searchKeyword"
              placeholder="快速搜索..."
              prefix-icon="Search"
              size="small"
              clearable
              class="search-input"
            />
          </div>

          <!-- 通知 -->
          <el-badge :value="3" class="notice-badge" type="danger">
            <el-icon :size="20" class="header-icon"><Bell /></el-icon>
          </el-badge>

          <!-- 全屏 -->
          <el-icon :size="20" class="header-icon" @click="toggleFullscreen">
            <FullScreen />
          </el-icon>

          <!-- 主题切换 -->
          <el-icon :size="20" class="header-icon" @click="toggleTheme">
            <Sunny v-if="isDark" />
            <Moon v-else />
          </el-icon>

          <!-- 用户下拉 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <el-avatar
                :size="36"
                :src="userStore.userInfo?.headImg"
                class="user-avatar"
              >
                {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <div class="dropdown-header">
                  <el-avatar
                    :size="48"
                    :src="userStore.userInfo?.headImg"
                  >
                    {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
                  </el-avatar>
                  <div class="dropdown-user-info">
                    <div class="dropdown-username">{{ userStore.userInfo?.username }}</div>
                    <div class="dropdown-role">{{ userRoleName }}</div>
                  </div>
                </div>
                <el-dropdown-item divided command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 标签页 -->
      <div class="tags-view">
        <div class="tags-wrapper">
          <div
            v-for="tag in visitedViews"
            :key="tag.path"
            class="tag-item"
            :class="{ active: tag.path === $route.path }"
            @click="router.push(tag.path)"
          >
            <el-icon class="tag-icon" v-if="tag.icon">
              <component :is="tag.icon" />
            </el-icon>
            <span class="tag-title">{{ tag.title }}</span>
            <el-icon class="tag-close" @click.stop="closeTag(tag)">
              <Close />
            </el-icon>
          </div>
        </div>
      </div>

      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Monitor,
  HomeFilled,
  ArrowDown,
  Bell,
  FullScreen,
  Sunny,
  Moon,
  User,
  Setting,
  SwitchButton,
  Close,
  Fold,
  Expand,
  Search
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 滚动状态
const isScrolled = ref(false)
const handleScroll = () => {
  isScrolled.value = window.scrollY > 10
}

// 暗黑模式
const isDark = ref(false)
const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

// 全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 搜索
const searchKeyword = ref('')

// 用户类型
const userType = computed(() => userStore.userInfo?.type ?? 0)

// 获取角色名称
const userRoleName = computed(() => {
  switch (userType.value) {
    case 1: return '管理员'
    case 2: return '维修员'
    default: return '普通用户'
  }
})

// 菜单路由（过滤隐藏的子路由和根据角色过滤）
const menuRoutes = computed(() => {
  const layoutRoute = router.getRoutes().find(r => r.name === 'Layout')
  if (!layoutRoute?.children) return []

  return layoutRoute.children.filter(r => {
    // 过滤隐藏的
    if (r.meta?.hidden) return false
    // 过滤角色权限
    const allowedRoles = r.meta?.roles as number[] | undefined
    if (allowedRoles && !allowedRoles.includes(userType.value)) return false
    return true
  })
})

// 标签页
const visitedViews = ref<Array<{ path: string; title: string; icon?: string }>>([
  { path: '/home', title: '首页', icon: 'HomeFilled' }
])

watch(
  () => route.path,
  (newPath) => {
    const exist = visitedViews.value.find(v => v.path === newPath)
    if (!exist && route.meta?.title) {
      visitedViews.value.push({
        path: newPath,
        title: route.meta.title as string,
        icon: route.meta.icon as string
      })
    }
  },
  { immediate: true }
)

const closeTag = (tag: { path: string }) => {
  const index = visitedViews.value.findIndex(v => v.path === tag.path)
  if (index > -1) {
    visitedViews.value.splice(index, 1)
    if (tag.path === route.path && visitedViews.value.length > 0) {
      router.push(visitedViews.value[Math.min(index, visitedViews.value.length - 1)].path)
    }
  }
}

// 用户菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
    case 'profile':
      router.push('/settings')
      break
    case 'settings':
      router.push('/settings')
      break
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  background: var(--bg-primary);
}

// 侧边栏
.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.1);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background:
      radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(118, 75, 162, 0.1) 0%, transparent 50%);
    pointer-events: none;
  }

  .logo {
    height: 70px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    cursor: pointer;
    position: relative;
    z-index: 1;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      background: rgba(255, 255, 255, 0.05);
    }

    .logo-icon {
      width: 40px;
      height: 40px;
      background: var(--gradient-blue);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      flex-shrink: 0;
      box-shadow: 0 4px 15px rgba(0, 118, 240, 0.4);
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.05) rotate(5deg);
      }
    }

    .logo-text {
      margin-left: 12px;
      overflow: hidden;

      h2 {
        margin: 0;
        font-size: 22px;
        font-weight: 700;
        background: linear-gradient(135deg, #fff 0%, #a8c5ff 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      p {
        margin: 2px 0 0;
        font-size: 11px;
        color: rgba(255, 255, 255, 0.5);
        letter-spacing: 1px;
      }
    }
  }

  .sidebar-menu {
    flex: 1;
    border-right: none;
    background: transparent;
    padding: 12px 0;
    overflow-y: auto;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 2px;
    }

    :deep(.el-menu-item) {
      color: rgba(255, 255, 255, 0.7);
      height: 50px;
      line-height: 50px;
      margin: 4px 12px;
      padding: 0 16px !important;
      border-radius: 10px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 3px;
        background: var(--gradient-blue);
        transform: scaleY(0);
        transition: transform 0.3s ease;
      }

      &:hover {
        color: white;
        background: rgba(255, 255, 255, 0.1);
        transform: translateX(4px);
      }

      &.is-active {
        color: white;
        background: linear-gradient(135deg, rgba(0, 118, 240, 0.3) 0%, rgba(0, 198, 255, 0.2) 100%);
        box-shadow: 0 4px 15px rgba(0, 118, 240, 0.3);

        &::before {
          transform: scaleY(1);
        }

        .menu-icon {
          transform: scale(1.1);
        }
      }

      .menu-icon {
        font-size: 20px;
        margin-right: 12px;
        transition: all 0.3s ease;
      }

      .menu-title {
        font-size: 14px;
        font-weight: 500;
      }
    }
  }

  .sidebar-footer {
    padding: 12px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    justify-content: center;

    .collapse-btn {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: rgba(255, 255, 255, 0.6);
      background: rgba(255, 255, 255, 0.05);
      transition: all 0.3s ease;

      &:hover {
        color: white;
        background: rgba(255, 255, 255, 0.1);
      }

      .rotate-180 {
        transform: rotate(180deg);
        transition: transform 0.3s ease;
      }
    }
  }

  // 收起状态样式
  &.sidebar-collapsed {
    .logo {
      padding: 0;
      justify-content: center;

      .logo-icon {
        margin: 0;
        transform: scale(1.1);

        &:hover {
          transform: scale(1.15) rotate(5deg);
        }
      }
    }

    .sidebar-menu {
      :deep(.el-menu-item) {
        margin: 6px 0;
        padding: 0 !important;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 0;
        width: 100%;

        &::before {
          left: 0;
          width: 4px;
          border-radius: 0 2px 2px 0;
        }

        &:hover {
          transform: scale(1.05);
          background: rgba(255, 255, 255, 0.12);
        }

        &.is-active {
          background: linear-gradient(135deg, rgba(0, 118, 240, 0.5) 0%, rgba(0, 198, 255, 0.4) 100%);
          box-shadow: 0 4px 20px rgba(0, 118, 240, 0.4), inset 0 0 0 1px rgba(255, 255, 255, 0.1);

          .menu-icon {
            transform: scale(1.2);
            filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.5));
          }
        }

        .menu-icon {
          margin-right: 0;
          font-size: 24px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
      }

      // 收起状态下的 tooltip 样式
      :deep(.el-tooltip) {
        padding: 0 !important;
        display: flex !important;
        align-items: center;
        justify-content: center;
      }
    }

    .sidebar-footer {
      .collapse-btn {
        width: 48px;
        height: 48px;
        border-radius: 12px;

        &:hover {
          transform: scale(1.1);
        }
      }
    }
  }
}

// 主内容区
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-primary);
}

// 头部
.header {
  height: 70px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid var(--border-light);
  transition: all 0.3s ease;
  position: sticky;
  top: 0;
  z-index: 100;

  &.header-scrolled {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .header-search {
      .search-input {
        width: 240px;
        transition: width 0.3s ease;

        &:focus-within {
          width: 300px;
        }
      }
    }

    .header-icon {
      color: var(--text-secondary);
      cursor: pointer;
      padding: 8px;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        color: var(--primary-color);
        background: rgba(64, 158, 255, 0.1);
        transform: scale(1.1);
      }
    }

    .notice-badge {
      :deep(.el-badge__content) {
        border: none;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      padding: 6px 12px;
      border-radius: 10px;
      transition: all 0.3s ease;

      &:hover {
        background: var(--bg-primary);
      }

      .user-avatar {
        background: var(--gradient-blue);
        color: white;
        font-weight: 600;
      }

      .user-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary);
      }
    }
  }
}

// 标签页
.tags-view {
  background: white;
  border-bottom: 1px solid var(--border-light);
  padding: 8px 24px;
  overflow-x: auto;

  &::-webkit-scrollbar {
    height: 0;
  }

  .tags-wrapper {
    display: flex;
    gap: 8px;

    .tag-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 14px;
      background: var(--bg-primary);
      border-radius: 6px;
      font-size: 13px;
      color: var(--text-secondary);
      cursor: pointer;
      transition: all 0.3s ease;
      border: 1px solid transparent;
      white-space: nowrap;

      .tag-icon {
        font-size: 14px;
      }

      .tag-close {
        font-size: 12px;
        opacity: 0;
        transition: all 0.2s ease;

        &:hover {
          color: #f56c6c;
          transform: scale(1.2);
        }
      }

      &:hover {
        background: rgba(64, 158, 255, 0.1);
        color: var(--primary-color);

        .tag-close {
          opacity: 1;
        }
      }

      &.active {
        background: linear-gradient(135deg, rgba(0, 118, 240, 0.1) 0%, rgba(0, 198, 255, 0.1) 100%);
        color: var(--primary-color);
        border-color: rgba(0, 118, 240, 0.3);
        font-weight: 500;

        .tag-close {
          opacity: 1;
        }
      }
    }
  }
}

// 主内容
.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--bg-primary);

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--border-color);
    border-radius: 4px;

    &:hover {
      background: var(--text-muted);
    }
  }
}

// 下拉菜单样式
:deep(.user-dropdown) {
  padding: 8px;
  min-width: 200px;

  .dropdown-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    margin-bottom: 8px;
    background: var(--bg-primary);
    border-radius: 8px;

    .dropdown-user-info {
      .dropdown-username {
        font-weight: 600;
        color: var(--text-primary);
        font-size: 14px;
      }

      .dropdown-role {
        font-size: 12px;
        color: var(--text-muted);
        margin-top: 2px;
      }
    }
  }

  .el-dropdown-menu__item {
    padding: 10px 16px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      font-size: 16px;
    }
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.page-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-leave-active {
  transition: all 0.2s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

// 响应式
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 1000;
    height: 100vh;
    transform: translateX(-100%);

    &.sidebar-open {
      transform: translateX(0);
    }
  }

  .header {
    padding: 0 16px;

    .header-search {
      display: none;
    }
  }

  .tags-view {
    padding: 8px 16px;
  }

  .main-content {
    padding: 16px;
  }
}
</style>
