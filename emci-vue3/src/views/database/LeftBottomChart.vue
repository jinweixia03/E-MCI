<template>
  <div id="centerRight2">
    <div class="bg-color-black">
      <div class="d-flex pt-2 pl-2">
        运维直辖域
      </div>
      <div class="operation">
        <div class="layout">
          <el-image :src="opt" class="image" />
          <span class="op-text">{{ o }}</span></div>
        <div class="layout">
          <el-image :src="sk" class="image" />
          <span class="op-text op-text-s">{{ s }}</span></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const opt = ref('/src/assets/opt.png')
const sk = ref('/src/assets/sk.png')
const o = ref(139)
const s = ref('13M')
</script>

<style lang="scss" scoped>
#centerRight2 {
  $box-height: 100%;
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

  .operation {
    width: 100%;
    height: 100%;
    line-height: 13vh;
    margin: 0 auto;
    text-align: center;
    font-size: 24px;

    .layout {
      display: flex;
      justify-content: center;
      align-items: center;

    }

    .image {
      width: 64px;
      height: 64px;
    }

    .op-text {
      display: inline-block;
      width: 30%;
      text-align: center;
      font-size: 36px;
      color: #1685fd;
    }

    .op-text-s {
      color: #06f9fb;
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
</style>