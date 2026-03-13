<template>
  <div>
    <div ref="chartRef" style="width: 100%; height: 500px;"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

interface Props {
  cdata?: Array<{ name: string; value: number }>
}

const props = withDefaults(defineProps<Props>(), {
  cdata: () => []
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      textStyle: {
        fontSize: 14,
        lineHeight: 22
      }
    },
    visualMap: {
      min: 0,
      max: 10,
      show: false,
      seriesIndex: 0,
      inRange: {
        color: ['rgba(41,166,206, .5)', 'rgba(69,117,245, .9)']
      }
    },
    series: [
      {
        name: '相关指数',
        type: 'scatter',
        symbolSize: 20,
        data: props.cdata.map(item => ({
          name: item.name,
          value: [Math.random() * 100, Math.random() * 100, item.value]
        }))
      }
    ]
  }

  chartInstance.setOption(option)
}

watch(() => props.cdata, () => {
  if (chartInstance) {
    initChart()
  }
}, { deep: true })

onMounted(() => {
  initChart()
})
</script>
