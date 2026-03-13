<template>
  <div id="centerRight2">
    <div class="bg-color-black">
      <div class="d-flex pt-2 pl-2">
        井盖安全评估
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

onMounted(() => {
  if (!chartRef.value) return

  const chart = echarts.init(chartRef.value)

  const option = {
    series: [
      {
        type: 'gauge',
        progress: {
          show: true,
          width: 10
        },
        axisLine: {
          lineStyle: {
            width: 10
          }
        },
        axisTick: {
          show: false
        },
        splitLine: {
          length: 10,
          lineStyle: {
            width: 2,
            color: '#999'
          }
        },
        axisLabel: {
          distance: 15,
          color: '#999',
          fontSize: 12
        },
        anchor: {
          show: true,
          showAbove: true,
          size: 15,
          itemStyle: {
            borderWidth: 10
          }
        },
        title: {
          show: false
        },
        detail: {
          valueAnimation: true,
          fontSize: 20,
          offsetCenter: [0, '60%']
        },
        data: [
          {
            value: 70
          }
        ]
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
    height: 27vh;
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
