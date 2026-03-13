<template>
  <div id="centerLeft1">
    <div class="bg-color-black">
      <div class="d-flex pt-2 pl-2">
        <span>
          <icon name="chart-pie" class="text-icon" />
        </span>
        <div class="d-flex">
          <span class="fs-xl text mx-2">地图数据</span>
          <dv-decoration-1 class="dv-dec-1" />
        </div>
      </div>
      <div class="d-flex jc-center">
        <CenterChartItem />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import CenterChartItem from './components/CenterChartItem/index.vue'
</script>

<style lang="scss" scoped>
#centerLeft1 {
  $box-width: 300px;
  $box-height: 745px;
  padding: 16px;
  height: calc(100vh - 175px);
  min-width: $box-width;
  border-radius: 5px;

  .bg-color-black {
    height: calc(100vh - 205px);
    border-radius: 10px;
  }

  .text {
    color: #c3cbde;
  }

  .dv-dec-1 {
    position: relative;
    width: 100px;
    height: 20px;
    top: -3px;
  }

  .chart-box {
    margin-top: 16px;
    width: 170px;
    height: 170px;

    .active-ring-name {
      padding-top: 10px;
    }
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

.jc-center {
  justify-content: center;
}
</style>