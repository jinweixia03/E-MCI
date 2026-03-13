<template>
  <div id="centerRight2">
    <div class="bg-color-black">
      <div class="d-flex pt-2 pl-2">
        模型AP评估
      </div>
      <div class="d-flex ai-center flex-column body-box">
        <LeftTopChartItem />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import LeftTopChartItem from './components/LeftTopChartItem/index.vue'
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

    .dv-cap-chart {
      width: 100%;
      height: 160px;
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

.ai-center {
  align-items: center;
}

.flex-column {
  flex-direction: column;
}
</style>
