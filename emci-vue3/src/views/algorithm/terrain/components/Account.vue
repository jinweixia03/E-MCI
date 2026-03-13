<template>
  <div class="account-container">
    <div class="image-compare">
      <div class="image-item">
        <h4>原始图片</h4>
        <el-image :src="details.original_img" fit="contain" />
      </div>
      <div class="image-item">
        <h4>地形检测结果</h4>
        <el-image :src="details.result_img" fit="contain" />
      </div>
    </div>
    <div class="actions">
      <el-button type="primary" @click="handleDownload">下载结果</el-button>
      <el-button @click="handleClose">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  details: {
    original_img: string
    result_img: string
    txt_url: string
    download_url: string
  }
}

defineProps<Props>()

const emit = defineEmits<{
  closeAccount: []
}>()

const handleDownload = () => {
  console.log('Downloading...')
}

const handleClose = () => {
  emit('closeAccount')
}
</script>

<style scoped>
.account-container {
  padding: 20px;
}

.image-compare {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.image-item {
  flex: 1;
  text-align: center;
}

.image-item h4 {
  margin-bottom: 10px;
  color: #303133;
}

.image-item .el-image {
  width: 100%;
  height: 400px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.actions {
  text-align: center;
  margin-top: 20px;
}
</style>
