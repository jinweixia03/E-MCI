<template>
  <div>
    <Echart
      id="centreRight2Chart1"
      :options="options"
      height="400px"
      width="300px"
    />
  </div>
</template>

<script setup lang="ts">
import Echart from '../../common/echart/index.vue'
import { ref, watch } from 'vue'

interface Props {
  cdata?: {
    indicatorData: { name: string; max: number }[]
    dataBJ: number[][]
    dataGZ: number[][]
  }
}

const props = withDefaults(defineProps<Props>(), {
  cdata: () => ({
    indicatorData: [],
    dataBJ: [],
    dataGZ: []
  })
})

const options = ref({})

watch(
  () => props.cdata,
  (newData) => {
    const lineStyle = {
      normal: {
        width: 1,
        opacity: 0.5
      }
    }

    options.value = {
      radar: {
        indicator: newData.indicatorData,
        shape: 'circle',
        splitNumber: 5,
        radius: ['0%', '65%'],
        name: {
          textStyle: {
            color: 'rgb(238, 197, 102)'
          }
        },
        splitLine: {
          lineStyle: {
            color: [
              'rgba(238, 197, 102, 0.1)',
              'rgba(238, 197, 102, 0.2)',
              'rgba(238, 197, 102, 0.4)',
              'rgba(238, 197, 102, 0.6)',
              'rgba(238, 197, 102, 0.8)',
              'rgba(238, 197, 102, 1)'
            ].reverse()
          }
        },
        splitArea: {
          show: false
        },
        axisLine: {
          lineStyle: {
            color: 'rgba(238, 197, 102, 0.5)'
          }
        }
      },
      series: [
        {
          name: '北京',
          type: 'radar',
          lineStyle: lineStyle,
          data: newData.dataBJ,
          symbol: 'none',
          itemStyle: {
            normal: {
              color: '#F9713C'
            }
          },
          areaStyle: {
            normal: {
              opacity: 0.1
            }
          }
        },
        {
          name: '上海',
          type: 'radar',
          lineStyle: lineStyle,
          data: newData.dataGZ,
          symbol: 'none',
          itemStyle: {
            normal: {
              color: '#B3E4A1'
            }
          },
          areaStyle: {
            normal: {
              opacity: 0.05
            }
          }
        }
      ]
    }
  },
  { immediate: true, deep: true }
)
</script>
