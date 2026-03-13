<template>
  <div class="home-page page-container">
    <!-- 欢迎横幅 -->
    <section class="welcome-banner">
      <div class="banner-content">
        <div class="banner-left">
          <div class="badge">
            <el-icon><Star /></el-icon>
            <span>智能检测系统 V2.0</span>
          </div>
          <h1 class="title">
            欢迎回来，<span class="highlight">{{ userStore.userInfo?.username || '用户' }}</span>
          </h1>
          <p class="subtitle">{{ greeting }}！{{ subtitleText }}</p>
          <div class="actions">
            <template v-if="isRepairer">
              <el-button type="primary" size="large" @click="$router.push('/repair')">
                <el-icon><Tools /></el-icon>查看任务
              </el-button>
            </template>
            <template v-else>
              <el-button type="primary" size="large" @click="$router.push('/detection')">
                <el-icon><VideoCamera /></el-icon>开始检测
              </el-button>
              <el-button size="large" @click="$router.push('/map')">
                <el-icon><MapLocation /></el-icon>查看地图
              </el-button>
            </template>
          </div>
        </div>
        <div class="banner-right">
          <img src="../../assets/logo/logo.png" alt="E-MCI" />
        </div>
      </div>
    </section>

    <!-- 统计卡片 -->
    <section class="stats-section">
      <div class="section-title">数据概览</div>
      <div class="stats-grid cols-4">
        <div v-for="stat in statistics" :key="stat.id" class="stat-card">
          <div class="stat-icon" :style="{ background: stat.gradient }">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-change" :class="stat.trend">
              <el-icon><ArrowUp v-if="stat.trend === 'up'" /><ArrowDown v-else /></el-icon>
              {{ stat.change }}%
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-actions">
      <div class="section-title">快捷入口</div>
      <div class="card-grid" :class="isRepairer ? 'cols-2' : 'cols-4'">
        <div
          v-for="action in filteredQuickActions"
          :key="action.id"
          class="action-card"
          @click="$router.push(action.path)"
        >
          <div class="action-icon" :style="{ background: action.gradient }">
            <el-icon><component :is="action.icon" /></el-icon>
          </div>
          <div class="action-content">
            <h3 class="action-title">{{ action.title }}</h3>
            <p class="action-desc">{{ action.desc }}</p>
          </div>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { dashboardApi } from '@/api/dashboard'
import type { DashboardStats } from '@/types/dashboard'
import {
  Star, VideoCamera, MapLocation, ArrowUp, ArrowDown, ArrowRight,
  Tools, Grid
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const stats = ref<DashboardStats | null>(null)

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await dashboardApi.getStats()
    stats.value = res.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
})

const isRepairer = computed(() => userStore.isRepairer)

const subtitleText = computed(() => {
  if (isRepairer.value) {
    return '请及时处理分配给你的维修任务'
  }
  const todayCount = stats.value?.detectionStats?.todayCount || 0
  return `系统运行正常，今日已检测 ${todayCount} 个井盖`
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息'
  if (hour < 9) return '早上好，开启新的一天'
  if (hour < 12) return '上午好，工作顺利'
  if (hour < 14) return '中午好，记得休息'
  if (hour < 18) return '下午好，继续加油'
  return '晚上好，今天辛苦了'
})

const statistics = computed(() => [
  {
    id: 1,
    icon: 'Location',
    value: stats.value?.manholeStats?.totalCount?.toString() || '0',
    label: '井盖总数',
    change: 12,
    trend: 'up',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    id: 2,
    icon: 'VideoCamera',
    value: (stats.value?.detectionStats?.totalCount || 0).toLocaleString(),
    label: '检测次数',
    change: 28,
    trend: 'up',
    gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)'
  },
  {
    id: 3,
    icon: 'Tools',
    value: (stats.value?.repairStats?.pendingCount || 0).toString(),
    label: '待维修',
    change: 5,
    trend: 'down',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    id: 4,
    icon: 'Grid',
    value: (stats.value?.droneStats?.totalCount || 0).toString(),
    label: '无人机',
    change: 2,
    trend: 'up',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
])

const quickActions = [
  {
    id: 1,
    title: '井盖地图',
    desc: '查看实时位置分布',
    icon: 'MapLocation',
    path: '/map',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    roles: [0, 1, 2] // 所有角色可见
  },
  {
    id: 2,
    title: 'AI检测',
    desc: '智能识别井盖状态',
    icon: 'VideoCamera',
    path: '/detection',
    gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    roles: [0, 1] // 普通用户和管理员可见
  },
  {
    id: 3,
    title: '维修调度',
    desc: '管理维修任务',
    icon: 'Tools',
    path: '/repair',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    roles: [0, 1, 2] // 所有角色可见
  },
  {
    id: 4,
    title: '数据大屏',
    desc: '可视化监控中心',
    icon: 'Monitor',
    path: '/dashboard',
    gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    roles: [0, 1] // 普通用户和管理员可见
  }
]

const filteredQuickActions = computed(() => {
  const userType = userStore.userInfo?.type ?? 0
  return quickActions.filter(action => action.roles.includes(userType))
})
</script>

<style scoped lang="scss">
// 使用全局布局变量，无需重复定义

// 欢迎横幅
.welcome-banner {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: var(--card-border-radius);
  padding: 32px 40px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Ccircle cx='30' cy='30' r='2'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  }

  .banner-content {
    display: grid;
    grid-template-columns: 1fr 280px;
    gap: 40px;
    align-items: center;
    position: relative;
    z-index: 1;
  }

  .banner-left {
    color: white;

    .badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 6px 14px;
      background: rgba(255,255,255,0.15);
      border-radius: 20px;
      font-size: 13px;
      margin-bottom: 16px;
      backdrop-filter: blur(10px);
    }

    .title {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 10px;
      line-height: 1.3;

      .highlight {
        background: linear-gradient(135deg, #ffd700, #ffaa00);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
    }

    .subtitle {
      font-size: 15px;
      opacity: 0.9;
      margin-bottom: 24px;
      line-height: 1.5;
    }

    .actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;

      .el-button {
        border-radius: 8px;
        padding: 12px 24px;
        font-size: 14px;
        height: 44px;

        &--primary {
          background: #409eff;
          color: white;
          border: none;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(0,0,0,0.2);
          }
        }

        &:not(&--primary) {
          background: rgba(255,255,255,0.15);
          color: white;
          border: 1px solid rgba(255,255,255,0.3);
        }
      }
    }
  }

  .banner-right {
    display: flex;
    justify-content: center;
    align-items: center;

    img {
      width: 240px;
      height: auto;
      filter: drop-shadow(0 20px 40px rgba(0,0,0,0.3));
      animation: float 6s ease-in-out infinite;
    }
  }
}

// 区块标题
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 4px solid #667eea;
}

// 快捷操作卡片
.action-card {
  background: var(--bg-secondary);
  border-radius: var(--card-border-radius-sm);
  padding: var(--card-padding);
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);

    .action-arrow {
      transform: translateX(4px);
    }
  }

  .action-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
    flex-shrink: 0;
  }

  .action-content {
    flex: 1;
    min-width: 0;

    .action-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 4px;
    }

    .action-desc {
      font-size: 12px;
      color: var(--text-muted);
      margin: 0;
    }
  }

  .action-arrow {
    color: var(--text-muted);
    transition: transform var(--transition-normal);
    flex-shrink: 0;
  }
}

// 动画
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

// ========================
// 响应式布局
// ========================

// 1440px 以下
@media (max-width: 1440px) {
  .welcome-banner {
    .banner-right img {
      width: 200px;
    }
  }
}

// 1200px 以下
@media (max-width: 1200px) {
  .welcome-banner {
    padding: 28px 32px;

    .banner-content {
      grid-template-columns: 1fr 180px;
    }

    .banner-left .title {
      font-size: 28px;
    }

    .banner-right img {
      width: 160px;
    }
  }
}

// 992px 以下
@media (max-width: 992px) {
  .welcome-banner {
    padding: 24px;

    .banner-content {
      grid-template-columns: 1fr;
    }

    .banner-right {
      display: none;
    }

    .banner-left {
      text-align: center;

      .actions {
        justify-content: center;
      }
    }
  }
}

// 768px 以下
@media (max-width: 768px) {
  .welcome-banner {
    padding: 20px;

    .banner-left {
      .title {
        font-size: 24px;
      }

      .subtitle {
        font-size: 14px;
      }

      .actions .el-button {
        padding: 10px 20px;
        height: 40px;
        font-size: 13px;
      }
    }
  }

  .section-title {
    font-size: 16px;
  }
}

// 480px 以下
@media (max-width: 480px) {
  .welcome-banner {
    padding: 16px;

    .banner-left {
      .badge {
        font-size: 11px;
        padding: 4px 10px;
      }

      .title {
        font-size: 20px;
      }

      .subtitle {
        font-size: 13px;
      }

      .actions {
        flex-direction: column;

        .el-button {
          width: 100%;
        }
      }
    }
  }

  .action-card {
    padding: 12px;

    .action-icon {
      width: 36px;
      height: 36px;
      font-size: 16px;
    }

    .action-content {
      .action-title {
        font-size: 14px;
      }

      .action-desc {
        font-size: 11px;
      }
    }
  }
}
</style>
