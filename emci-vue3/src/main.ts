import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import DataVVue3 from '@kjgl77/datav-vue3'

// 引入全局样式
import './styles/index.scss'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// Pinia
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// Element Plus Icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(DataVVue3)

app.mount('#app')
