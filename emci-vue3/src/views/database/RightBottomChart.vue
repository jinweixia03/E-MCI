<template>
  <div id="centerRight2">
    <div class="bg-color-black">
      <div class="d-flex pt-2 pl-2">
        数据报表生成
      </div>
      <div class="d-flex excel">
        <div class="excel-div" @click="handeDownLoadWord">
          <el-icon :size="32" color="#007aff"><Document /></el-icon>
          <span>Word</span>
        </div>
        <div class="just" @click="handeDownLoadExcel">
          <el-icon :size="32" color="#4ec219"><Files /></el-icon>
          <span>Excel</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Document, Files } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const handeDownLoadWord = () => {
  ElMessage.info('下载Word功能开发中')
}

const handeDownLoadExcel = () => {
  ElMessage.info('下载Excel功能开发中')
}
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

  .excel {
    width: 100%;
    height: calc(100% - 28px);
    line-height: 60px;
    justify-content: center;
    align-items: center;
    font-size: 24px;

    .excel-icon {
      margin-right: 8px;
      font-size: 32px;
      color: #4ec219;
    }

    .excel-div {
      margin-right: 32px;
      align-content: center;
      cursor: pointer;

      &:hover {
        opacity: 0.8;
      }

      .excel-icon {
        color: #007aff;
      }
    }

    .just {
      display: flex;
      align-items: center;
      cursor: pointer;

      &:hover {
        opacity: 0.8;
      }
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
