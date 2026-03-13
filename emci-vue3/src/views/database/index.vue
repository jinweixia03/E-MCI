<template>
  <div id="index" ref="appRef">
    <div class="bg">
      <dv-loading v-if="loading">Loading...</dv-loading>
      <div v-else class="host-body">
        <div class="d-flex jc-center">
          <dv-decoration-10 class="dv-dec-10" />
          <div class="d-flex jc-center">
            <dv-decoration-8 class="dv-dec-8" :color="decorationColor" />
            <div class="title">
              <span class="title-text"> 光缆井盖运维管理可视化看板</span>
              <dv-decoration-6
                class="dv-dec-6"
                :reverse="true"
                :color="['#50e3c2', '#67a1e5']"
              />
            </div>
            <dv-decoration-8
              class="dv-dec-8"
              :reverse="true"
              :color="decorationColor"
            />
          </div>
          <dv-decoration-10 class="dv-dec-10-s" />
        </div>
        <div class="year-layout">
          <span class="react-after" />
          <span>{{ dateYear }} {{ dateWeek }} {{ dateDay }}</span>
          <span style="padding-left: 36px">天气：{{ weather }}
            <span style="padding-left: 8px">{{ temperature }}℃</span></span>
        </div>
        <el-row class="content-box" :gutter="24">
          <el-col :span="6">
            <div class="left-layout">
              <dv-border-box-12>
                <LeftTopChart />
              </dv-border-box-12>
              <dv-border-box-13 style="padding: 12px 8px">
                <LeftBottomChart />
              </dv-border-box-13>
            </div>
          </el-col>
          <el-col :span="10">
            <dv-border-box-12>
              <CenterChart />
            </dv-border-box-12>
          </el-col>
          <el-col :span="8">
            <div class="right-chart">
              <dv-border-box-13 style="padding: 12px 8px">
                <RightTopChart />
              </dv-border-box-13>
              <dv-border-box-12>
                <RightMiddleChart />
              </dv-border-box-12>
              <dv-border-box-12>
                <RightBottomChart />
              </dv-border-box-12>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { formatTime } from './utils'
import CenterChart from './CenterChart.vue'
import LeftTopChart from './LeftTopChart.vue'
import LeftBottomChart from './LeftBottomChart.vue'
import RightTopChart from './RightTopChart.vue'
import RightMiddleChart from './RightMiddleChart.vue'
import RightBottomChart from './RightBottomChart.vue'

const loading = ref(true)
const dateDay = ref('')
const dateYear = ref('')
const dateWeek = ref('')
const weekday = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const decorationColor = ['#568aea', '#50e3c2']
const temperature = ref('25')
const weather = ref('晴')

let timing: ReturnType<typeof setInterval> | null = null

const timeFn = () => {
  timing = setInterval(() => {
    dateDay.value = formatTime(new Date(), 'HH: mm: ss')
    dateYear.value = formatTime(new Date(), 'yyyy年MM月dd日')
    dateWeek.value = weekday[new Date().getDay()]
  }, 1000)
}

const cancelLoading = () => {
  setTimeout(() => {
    loading.value = false
  }, 500)
}

onMounted(async () => {
  timeFn()
  cancelLoading()
  // 天气功能暂时使用静态数据
})

onUnmounted(() => {
  if (timing) {
    clearInterval(timing)
  }
})
</script>

<style lang="scss" scoped>
#index {
  color: #d3d6dd;
  width: 100%;
  height: calc(100vh - 90px);
  position: absolute;
  overflow: hidden;

  .bg {
    width: 100%;
    height: 100%;
    padding: 0;
    background-color: #0d1943;
    background-size: cover;
    background-position: center center;
  }

  .host-body {
    padding-top: 10px;

    .dv-dec-10,
    .dv-dec-10-s {
      width: 33.3%;
      height: 5px;
    }

    .dv-dec-10-s {
      transform: rotateY(180deg);
    }

    .dv-dec-8 {
      width: 200px;
      height: 50px;
    }

    .title {
      position: relative;
      width: 500px;
      text-align: center;
      background-size: cover;
      background-repeat: no-repeat;

      .title-text {
        width: 100%;
        font-size: 24px;
        position: absolute;
        bottom: 18px;
        transform: translate(-50%);
      }

      .dv-dec-6 {
        position: absolute;
        bottom: -5px;
        left: 50%;
        width: 250px;
        height: 8px;
        transform: translate(-50%);
      }
    }

    .content-box {
      margin-top: 16px;
      width: 100%;
      padding: 0 3%;
    }
  }
}

.year-layout {
  position: absolute;
  top: 62px;
  right: 5%;
  font-size: 12px;
  color: #4b5fad;
}

.left-layout {
  height: calc(100vh - 175px);
  display: grid;
  grid-template-rows: 3fr 2fr;
}

.right-chart {
  height: calc(100vh - 165px);
  display: grid;
  grid-template-rows: 2fr 2fr 1fr;
}

.d-flex {
  display: flex;
}

.jc-center {
  justify-content: center;
}
</style>
