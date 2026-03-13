<template>
  <div class="app-container">
    <el-card class="info-box">
      <div class="info-search">
        <span class="info-title">地区选择：</span>
        <el-form ref="formRef" :model="form" size="default" :inline="true">
          <el-form-item>
            <el-select v-model="form.province" placeholder="省级" @change="provinceChange">
              <el-option
                v-for="item in provinceSelect"
                :key="item.code"
                :label="item.name"
                :value="item.code"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="form.city" placeholder="市级" @change="cityChange">
              <el-option
                v-for="item in citySelect"
                :key="item.code"
                :label="item.name"
                :value="item.code"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select v-model="form.area" placeholder="区级" @change="areaChange">
              <el-option
                v-for="item in areaSelect"
                :key="item.code"
                :label="item.name"
                :value="item.code"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <el-card>
      <el-table
        size="default"
        :data="showData"
        style="width: 100%">
        <el-table-column
            prop="code"
            label="井盖编号"
            align="center"
          />
          <el-table-column
            prop="number"
            label="维修次数"
            align="center"
          />
          <el-table-column
            prop="indicators"
            label="安全指标"
            align="center"
          />
        <el-table-column
          align="center"
          label="操作">
          <template #default="scope">
            <el-button type="primary" size="default" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="info-layout">
        <el-pagination
          background
          @current-change="handleCurrentChange"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 模拟地区数据
const region = {
  province: [
    { code: '350000', name: '福建省' }
  ],
  city: [
    { code: '350100', name: '福州市', provinceCode: '350000' },
    { code: '350200', name: '厦门市', provinceCode: '350000' },
    { code: '350300', name: '莆田市', provinceCode: '350000' }
  ],
  area: [
    { code: '350102', name: '鼓楼区', cityCode: '350100' },
    { code: '350103', name: '台江区', cityCode: '350100' },
    { code: '350104', name: '仓山区', cityCode: '350100' }
  ]
}

const form = reactive({
  province: '',
  city: '',
  area: ''
})

const tableData = ref<any[]>([])
const showData = ref<any[]>([])
const pageSize = ref(10)
const total = ref(0)

const provinceSelect = computed(() => region.province)
const citySelect = computed(() => {
  const code = region.province.find(item => item.code === form.province)?.code
  return region.city.filter(item => item.provinceCode === code)
})
const areaSelect = computed(() => {
  const code = region.city.find(item => item.code === form.city)?.code
  return region.area.filter(item => item.cityCode === code)
})

const getManholeList = () => {
  // 模拟数据
  const mockData = []
  for (let i = 1; i <= 50; i++) {
    mockData.push({
      code: `MH${String(i).padStart(4, '0')}`,
      number: Math.floor(Math.random() * 20),
      indicators: (Math.random() * 100).toFixed(2)
    })
  }
  total.value = mockData.length
  tableData.value = mockData
  showData.value = mockData.slice(0, pageSize.value)
}

const provinceChange = () => {
  form.city = ''
  form.area = ''
}

const cityChange = () => {
  form.area = ''
}

const areaChange = () => {
  ElMessage.info('地区筛选功能开发中')
}

const handleView = (row: any) => {
  router.push('/information/detail/' + row.code)
}

const handleCurrentChange = (index: number) => {
  showData.value = tableData.value.slice((index - 1) * pageSize.value, index * pageSize.value)
}

onMounted(() => {
  getManholeList()
})
</script>

<style lang="scss" scoped>
.app-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding: 20px;
}

.info-box {
  margin-bottom: 20px;

  .info-title {
    color: #1f2d3d;
    font-size: 18px;
    font-weight: 600;
    margin-top: 10px;
  }

  .info-search {
    display: flex;
    justify-content: flex-start;
    flex-direction: row;
    align-items: center;
  }
}

.info-layout {
  margin: 8px 0;
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
}
</style>
