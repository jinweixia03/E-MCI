<template>
  <div>
    <Chart :cdata="cdata" />
  </div>
</template>

<script setup lang="ts">
import Chart from './chart.vue'
import { reactive } from 'vue'

const cdata = reactive([
  {
    name: '福州市',
    value: 10,
    elseData: {}
  },
  {
    name: '厦门市',
    value: 9
  },
  {
    name: '漳州市',
    value: 8
  },
  {
    name: '泉州市',
    value: 7
  },
  {
    name: '三明市',
    value: 6
  },
  {
    name: '莆田市',
    value: 5
  },
  {
    name: '南平市',
    value: 4
  },
  {
    name: '龙岩市',
    value: 3
  },
  {
    name: '宁德市',
    value: 2
  }
])
</script>

<style lang="scss" scoped>
</style>