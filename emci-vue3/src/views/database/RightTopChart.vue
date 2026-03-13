<template>
  <div id="centerRight2">
    <div class="bg-color-black">
      <div class="d-flex pt-2 pl-2">
        异常类别分析
      </div>
      <div class="d-flex ai-center flex-column body-box">
        <div ref="chartRef" style="width: 100%; height: 200px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLDivElement>()

const data = [
  { value: 1048, name: '正常' },
  { value: 735, name: '损坏' },
  { value: 580, name: '缺失' },
  { value: 484, name: '翘起' },
  { value: 300, name: '井圈' }
]

onMounted(() => {
  if (!chartRef.value) return

  const chart = echarts.init(chartRef.value)

  const option = {
    color: ['#451dff', '#176cfd', '#078afc', '#06f9fb', '#26cfa7'],
    title: {
      text: data.reduce((a, b) => a + b.value, 0),
      subtext: '总览',
      textStyle: {
        fontSize: 24,
        color: '#06f9fb',
        fontWeight: 600
      },
      subtextStyle: {
        fontSize: 24,
        color: '#fff',
        fontWeight: 600
      },
      textAlign: 'center',
      x: '61%',
      y: '39%'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '20%',
      left: '0',
      selectedMode: false,
      align: 'left',
      orient: 'vertical'
    },
    series: [
      {
        left: '25%',
        type: 'pie',
        radius: ['60%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          color: '#fff',
          formatter: (param: any) => `${param.percent * 2}%`
        },
        labelLine: {
          show: true,
          showAbove: true
        },
        data: data
      }
    ]
  }

  chart.setOption(option)
})
</script>

<style lang="scss" scoped>
#centerRight2 {
  $box-height: auto;
  $box-width: 100%;
  padding: 5px;
  height: $box-height;
  width: $box-width;
  border-radius: 5px;

  .bg-color-black {
    padding: 5px;
    height: $box-height;
    width: $box-width;
    border-radius: 10px;
  }

  .text {
    color: #c3cbde;
  }

  .body-box {
    border-radius: 10px;
    overflow: hidden;
  }
}

.d-flex {
  display: flex;
}

.pt-2 {
  padding-top: 0.5rem;
}

.pl-2 {
  padding-left: 0.5rem;
}

.ai-center {
  align-items: center;
}

.flex-column {
  flex-direction: column;
}
</style>
