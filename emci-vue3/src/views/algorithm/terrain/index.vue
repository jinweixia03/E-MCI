<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <el-card v-if="!showDetails" style="margin-bottom:20px;">
          <div class="profile-upload">
            <el-upload
              class="upload-demo"
              drag
              :action="Url"
              :show-file-list="false"
              :before-upload="DetImagesVideo"
            >
              <el-icon :size="67" class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">点击或将文件拖拽到这里<em>上传</em></div>
              <template #tip>
                <div class="el-upload__tip">支持扩展名：.png .jpeg .jpg .mp4...（地形检测）</div>
              </template>
            </el-upload>
          </div>
        </el-card>
        <Account v-else :details="details" @closeAccount="closeAccount" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import Account from './components/Account.vue'

const showDetails = ref(false)
const Url = ref('')
const details = ref({
  original_img: '',
  result_img: '',
  txt_url: '',
  download_url: '',
})

const DetImagesVideo = (file: File) => {
  console.log('Uploading file:', file)
  return false
}

const closeAccount = () => {
  showDetails.value = false
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.profile-upload {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

:deep(.el-upload-dragger) {
  width: 400px;
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
</style>
