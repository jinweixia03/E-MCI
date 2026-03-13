<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="400px"
    :before-close="handleClose"
  >
    <el-form :model="form" label-width="80px">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'

interface Props {
  dialogVisible: boolean
}

const props = defineProps<Props>()
const emit = defineEmits(['update:dialogVisible', 'handleClose'])

const visible = computed({
  get: () => props.dialogVisible,
  set: (val) => emit('update:dialogVisible', val)
})

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const handleClose = () => {
  emit('handleClose')
}

const handleSubmit = () => {
  if (!form.oldPassword || !form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  ElMessage.success('密码修改成功')
  handleClose()
}
</script>
