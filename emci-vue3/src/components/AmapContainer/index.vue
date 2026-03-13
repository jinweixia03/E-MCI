<template>
  <div ref="mapContainer" class="amap-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

const props = defineProps<{
  center?: [number, number]
  zoom?: number
}>()

const emit = defineEmits<{
  (e: 'mapLoaded', map: any): void
  (e: 'markerClick', data: any): void
}>()

const mapContainer = ref<HTMLDivElement>()
let map: any = null
let AMap: any = null

// 安全密钥配置
window._AMapSecurityConfig = {
  securityJsCode: 'a8cc4b1f9059d7e80c3b2c28ee3e9e31'
}

onMounted(async () => {
  try {
    AMap = await AMapLoader.load({
      key: '0713d505f8ee48a9c7fe9d43f7e2fef5',
      version: '2.0',
      plugins: ['AMap.Marker', 'AMap.InfoWindow', 'AMap.Scale', 'AMap.ToolBar']
    })

    map = new AMap.Map(mapContainer.value!, {
      zoom: props.zoom || 12,
      center: props.center || [121.4737, 31.2304], // 上海
      viewMode: '2D'
    })

    // 添加控件
    map.addControl(new AMap.Scale())
    map.addControl(new AMap.ToolBar())

    emit('mapLoaded', map)
  } catch (error) {
    console.error('地图加载失败:', error)
  }
})

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
})

// 添加标记点
const addMarker = (position: [number, number], title?: string, data?: any) => {
  if (!map || !AMap) return

  const marker = new AMap.Marker({
    position,
    title,
    clickable: true
  })

  marker.on('click', () => {
    emit('markerClick', data)
  })

  map.add(marker)
  return marker
}

// 设置地图中心
const setCenter = (center: [number, number]) => {
  if (map) {
    map.setCenter(center)
  }
}

// 清除所有标记
const clearMarkers = () => {
  if (map) {
    map.clearMap()
  }
}

defineExpose({
  addMarker,
  setCenter,
  clearMarkers,
  getMap: () => map
})
</script>

<style scoped>
.amap-container {
  width: 100%;
  height: 100%;
  min-height: 500px;
}
</style>
