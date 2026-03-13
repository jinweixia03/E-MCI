<template>
  <div :id="id" :class="className" :style="{ height: height, width: width }" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import tdTheme from './theme.json'

interface Props {
  className?: string
  id?: string
  width?: string
  height?: string
  options?: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  className: 'chart',
  id: 'chart',
  width: '100%',
  height: '2.5rem',
  options: () => ({})
})

const chart = ref<echarts.ECharts | null>(null)

const initChart = () => {
  const chartDom = document.getElementById(props.id)
  if (!chartDom) return

  echarts.registerTheme('tdTheme', tdTheme as any)
  chart.value = echarts.init(chartDom, 'tdTheme')
  chart.value.setOption(props.options, true)

  const handleResize = (e: any) => {
    if (chart.value) {
      chart.value.resize({
        width: e.target.innerWidth * 0.2,
        height: e.target.innerHeight * 0.25
      })
    }
  }

  window.addEventListener('resize', handleResize)

  return () => {
    window.removeEventListener('resize', handleResize)
  }
}

watch(
  () => props.options,
  (newOptions) => {
    if (chart.value) {
      chart.value.setOption(newOptions, true)
    }
  },
  { deep: true }
)

onMounted(() => {
  initChart()
})

onUnmounted(() => {
  if (chart.value) {
    chart.value.dispose()
    chart.value = null
  }
})
</script>
