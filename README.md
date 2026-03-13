<div align="center">

<img src="logo.png" alt="EMCI Logo" width="150" height="150" style="border-radius: 20px;">

# 🕳️ EMCI - 智能井盖检测管理系统

<p>
  <b>基于 YOLO 深度学习的井盖缺陷智能检测与运维管理平台</b>
</p>

<p>
  <a href="https://spring.io/projects/spring-boot">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.2.3-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot">
  </a>
  <a href="https://vuejs.org/">
    <img src="https://img.shields.io/badge/Vue.js-3.4-4FC08D?logo=vue.js&logoColor=white" alt="Vue.js">
  </a>
  <a href="https://www.typescriptlang.org/">
    <img src="https://img.shields.io/badge/TypeScript-5.3-3178C6?logo=typescript&logoColor=white" alt="TypeScript">
  </a>
  <a href="https://onnx.ai/">
    <img src="https://img.shields.io/badge/ONNX%20Runtime-1.16-005CED?logo=onnx&logoColor=white" alt="ONNX">
  </a>
</p>

<p>
  <img src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Python-3.10-3776AB?logo=python&logoColor=white" alt="Python">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis&logoColor=white" alt="Redis">
</p>

<p>
  <a href="#-快速开始">🚀 快速开始</a> •
  <a href="#-功能介绍">✨ 功能介绍</a> •
  <a href="#-用户指南">📖 用户指南</a> •
  <a href="#-api文档">🔌 API 文档</a> •
  <a href="#-项目结构">📁 项目结构</a>
</p>

<img src="https://placehold.co/900x450/1a1f3a/FFFFFF?text=Dashboard+Preview" alt="Dashboard Preview" width="900" style="border-radius: 10px; box-shadow: 0 10px 30px rgba(0,0,0,0.3);">

</div>

---

## 📋 目录

- [🎯 项目简介](#-项目简介)
- [✨ 功能介绍](#-功能介绍)
- [🏗️ 技术架构](#️-技术架构)
- [🚀 快速开始](#-快速开始)
  - [环境要求](#环境要求)
  - [方法一：使用 deploy.bat 一键启动（推荐Windows）](#方法一使用-deploybat-一键启动推荐windows)
  - [方法二：手动启动](#方法二手动启动)
  - [方法三：Docker 部署](#方法三docker-部署)
- [⚙️ 配置详解](#️-配置详解)
- [📖 用户指南](#-用户指南)
- [🔌 API 文档](#-api-文档)
- [📁 项目结构](#-项目结构)
- [❓ 常见问题](#-常见问题)
- [📝 更新日志](#-更新日志)
- [📄 许可证](#-许可证)

---

## 🎯 项目简介

**EMCI** (Smart Manhole Inspection System) 是一款面向城市基础设施管理的智能化解决方案。系统整合了计算机视觉、GIS 地理信息和物联网技术，实现井盖资产的数字化管理和维护流程的智能化调度。

### 💡 核心痛点与解决方案

| 痛点 | 传统方式 | EMCI 解决方案 |
|------|---------|--------------|
| 🔍 检测效率低 | 人工巡检，耗时耗力 | **AI 自动识别** - 秒级检测，批量处理 |
| 📊 数据孤岛 | Excel 表格，难以统计 | **云端数字化** - 实时同步，多维分析 |
| 📍 定位不精准 | 文字描述，难以寻找 | **GPS 精准定位** - 地图标注，导航直达 |
| 🔔 响应滞后 | 电话报修，流程繁琐 | **智能派单** - 自动分配，实时追踪 |
| 📈 决策缺乏依据 | 手工汇总，滞后严重 | **实时数据大屏** - 一目了然，科学决策 |

---

## ✨ 功能介绍

### 👥 用户角色与权限

系统支持四种角色，不同角色拥有不同的功能权限：

| 角色 | 权限范围 | 主要职责 |
|------|---------|---------|
| 👨‍💼 **管理员** | 全部功能 | 系统管理、用户管理、数据查看、所有操作 |
| 🔍 **检测员** | 检测相关 | 上传图片检测、查看检测结果、管理井盖信息 |
| 🔧 **维修员** | 维修相关 | 查看维修任务、更新维修进度、提交完成报告 |
| 👤 **普通用户** | 查看权限 | 查看地图、查看统计、个人中心 |

### 📱 功能模块详解

#### 1️⃣ 数据大屏
**实时监控，一目了然**

- 📊 井盖概况统计（总数、正常、损坏、维修中）
- 📈 近7天检测趋势图
- 🥧 井盖状态分布饼图
- 📊 井盖类型分布柱状图
- 🐛 缺陷类型分布分析
- 🔧 维修任务统计
- 🚁 无人机状态监控
- 📝 实时检测记录列表

#### 2️⃣ 井盖地图
**GIS 可视化，精准管理**

- 🗺️ 高德地图集成，支持缩放、拖拽
- 📍 井盖点位标注，不同状态不同颜色
- 🔍 区域聚合显示，海量数据不拥挤
- 🎯 点击查看井盖详细信息
- 🚁 无人机巡检路径规划
- 🎨 状态筛选：全部/正常/损坏/维修中

#### 3️⃣ AI 智能检测
**深度学习，精准识别**

- 🤖 基于 YOLOv8 的目标检测算法
- 📤 支持单张/批量图片上传
- 🔍 自动识别缺陷类型：
  - 破损（Crack）
  - 移位（Shift）
  - 缺失（Missing）
  - 异物（Foreign）
  - 下沉（Sunken）
- 📊 置信度评分，辅助决策
- 🖼️ 检测结果可视化标注

#### 4️⃣ 井盖管理
**资产数字化，全生命周期管理**

- 📝 井盖信息 CRUD
- 📍 GPS 坐标管理
- 🏷️ 类型分类（雨水/污水/电力/通信等）
- 📊 状态追踪（正常/损坏/维修中）
- 📸 历史检测记录关联
- 📥 批量导入/导出 Excel

#### 5️⃣ 维修管理
**工单流程化，进度可视化**

- 📋 维修工单自动/手动创建
- 🔔 实时通知维修员
- 📊 维修进度追踪（待处理→进行中→待确认→已完成）
- 📸 维修前后对比图
- ⭐ 维修质量评价
- 📈 维修统计报表

#### 6️⃣ 无人机管理
**智能巡检，自动规划**

- 🚁 无人机状态监控
- 📍 地图上拖拽设置巡检位置
- ⭕ 可视化设置巡检半径
- 🛤️ 自动规划最优巡检路径
- 📊 覆盖井盖数量估算
- ⏱️ 预计巡检时间计算

#### 7️⃣ 数据分析
**多维统计，辅助决策**

- 📊 检测数据统计（日/周/月）
- 🐛 缺陷类型占比分析
- 🗺️ 区域分布热力图
- 📈 趋势分析与预测
- 📑 报表导出（PDF/Excel）

#### 8️⃣ 个人中心
**个性化设置，便捷管理**

- 👤 个人信息修改
- 🔒 密码修改
- 🖼️ 头像上传
- 📊 个人工作统计

---

## 🏗️ 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              🎨 前端层                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐ │
│  │   Vue 3     │  │ Element Plus│  │   Pinia     │  │  Vue Router     │ │
│  │  TypeScript │  │   UI 组件   │  │  状态管理   │  │   路由管理      │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────┘ │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                       │
│  │  ECharts    │  │  DataV Vue3 │  │  高德地图   │                       │
│  │  数据图表   │  │  数据大屏   │  │   API      │                       │
│  └─────────────┘  └─────────────┘  └─────────────┘                       │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           🌐 Nginx 网关层                                │
│              反向代理 / 负载均衡 / 静态资源托管 / SSL 终端                │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              ⚙️ 后端层                                   │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                    Spring Boot 3.2.3                            │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │    │
│  │  │   Web    │ │  Security│ │ Validation│ │   MyBatis Plus   │   │    │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │    │
│  │  │   JWT    │ │  Redis   │ │   AOP    │ │   Knife4j 文档   │   │    │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌─────────────────────┐ ┌─────────────────┐ ┌─────────────────────┐
│    🗄️ MySQL 8.0     │ │   ⚡ Redis 7.x   │ │   🤖 AI Detection   │
│    业务数据存储      │ │   缓存/会话/锁   │ │   YOLO + ONNX       │
└─────────────────────┘ └─────────────────┘ └─────────────────────┘
```

### 技术栈详情

#### 后端技术 (emci-new)

| 技术 | 版本 | 用途 |
|------|------|------|
| [Spring Boot](https://spring.io/projects/spring-boot) | 3.2.3 | 核心应用框架 |
| [Spring Security](https://spring.io/projects/spring-security) | 6.2.x | 安全认证与授权 |
| [MyBatis Plus](https://baomidou.com/) | 3.5.5 | ORM 数据访问 |
| [JWT](https://github.com/auth0/java-jwt) | 4.4.0 | Token 身份认证 |
| [Redis](https://redis.io/) | 7.x | 缓存、会话、分布式锁 |
| [Knife4j](https://doc.xiaominfo.com/) | 4.4.0 | API 文档自动生成 |
| [Hutool](https://hutool.cn/) | 5.8.23 | Java 工具库 |
| [Lombok](https://projectlombok.org/) | 1.18.30 | 代码简化 |

#### 前端技术 (emci-vue3)

| 技术 | 版本 | 用途 |
|------|------|------|
| [Vue](https://vuejs.org/) | 3.4.19 | 渐进式前端框架 |
| [TypeScript](https://www.typescriptlang.org/) | 5.3.3 | 静态类型检查 |
| [Vite](https://vitejs.dev/) | 5.1.x | 下一代构建工具 |
| [Element Plus](https://element-plus.org/) | 2.5.6 | 企业级 UI 组件库 |
| [Pinia](https://pinia.vuejs.org/) | 2.1.7 | 状态管理方案 |
| [Vue Router](https://router.vuejs.org/) | 4.3.0 | 客户端路由 |
| [ECharts](https://echarts.apache.org/) | 5.5.0 | 数据可视化图表 |
| [Axios](https://axios-http.com/) | 1.6.7 | HTTP 客户端 |

#### AI 检测技术

| 技术 | 用途 |
|------|------|
| [YOLOv8](https://github.com/ultralytics/ultralytics) | 目标检测算法 |
| [ONNX Runtime](https://onnxruntime.ai/) | 跨平台高性能推理 |
| [OpenCV](https://opencv.org/) | 图像处理与预处理 |
| Python 3.10 | AI 推理服务 |

---

## 🚀 快速开始

### 环境要求

在开始之前，请确保你的系统满足以下要求：

| 依赖 | 版本要求 | 下载地址 | 验证命令 |
|------|---------|---------|---------|
| JDK | 17+ | [下载](https://adoptium.net/) | `java -version` |
| Maven | 3.8+ | [下载](https://maven.apache.org/) | `mvn -v` |
| Node.js | 18+ | [下载](https://nodejs.org/) | `node -v` |
| MySQL | 8.0+ | [下载](https://dev.mysql.com/) | `mysql --version` |
| Redis | 7.0+ | [下载](https://redis.io/download/) | `redis-cli --version` |
| Python | 3.10+ | [下载](https://www.python.org/) | `python --version` |

### 方法一：使用 deploy.bat 一键启动（推荐Windows）

项目根目录提供了 `deploy.bat` 脚本，可一键启动前后端服务：

```powershell
# 1. 克隆项目
git clone <your-repository-url>
cd emic

# 2. 修改 deploy.bat 中的配置（首次使用）
# 编辑 deploy.bat，修改以下变量为你的实际路径：
# - BACKEND_DIR: 后端项目路径
# - FRONTEND_DIR: 前端项目路径
# - MAVEN_CMD: Maven 命令路径

# 3. 双击运行 deploy.bat
# 或在命令行执行：
.\deploy.bat
```

**deploy.bat 功能说明：**

| 功能 | 说明 |
|------|------|
| ✅ 环境检查 | 自动检查 Java、Maven、Node.js 是否安装 |
| 🧹 端口清理 | 自动关闭占用端口（8087、5173）的旧进程 |
| 🔨 自动编译 | 首次运行自动编译后端代码 |
| 📦 依赖安装 | 自动安装前端 npm 依赖 |
| 🚀 一键启动 | 同时启动前后端服务 |
| 🛑 一键停止 | 按任意键停止所有服务 |

**访问地址：**
- 前端页面：http://localhost:5173
- 后端 API：http://localhost:8087/api
- API 文档：http://localhost:8087/api/doc.html

### 方法二：手动启动

#### Step 1: 数据库配置

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS emci
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

# 3. 执行初始化脚本
USE emci;
source emci.sql;
```

#### Step 2: Redis 配置

```bash
# 启动 Redis 服务
redis-server

# 或使用 Windows 服务
net start redis
```

#### Step 3: 后端配置与启动

```bash
# 进入后端目录
cd emci-new

# 配置数据库（编辑配置文件）
notepad src/main/resources/application.yml
```

**application.yml 关键配置：**

```yaml
server:
  port: 8087  # 后端服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/emci?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root      # 修改为你的数据库用户名
    password: 123456    # 修改为你的数据库密码

  data:
    redis:
      host: localhost   # Redis 地址
      port: 6379        # Redis 端口
      password:         # Redis 密码（如果有）

# JWT 配置
app:
  jwt:
    secret: your-secret-key-here-change-in-production
    expiration: 24      # Token 有效期（小时）
```

```bash
# 编译并运行后端
.\mvnw clean compile
.\mvnw spring-boot:run

# 或打包后运行
.\mvnw clean package -DskipTests
java -jar target/emci-new-1.0.0.jar
```

后端服务启动成功标志：
```
Started EmciApplication in x.x seconds
Tomcat started on port(s): 8087
```

#### Step 4: 前端配置与启动

```bash
# 进入前端目录
cd emci-vue3

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端服务启动成功标志：
```
VITE v5.x.x  ready in xxx ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

### 方法三：Docker 部署

```bash
# 1. 确保已安装 Docker 和 Docker Compose

# 2. 在项目根目录执行
docker-compose -f docker-compose-new.yml up -d

# 3. 查看服务状态
docker-compose -f docker-compose-new.yml ps

# 4. 停止服务
docker-compose -f docker-compose-new.yml down
```

**Docker 部署包含的服务：**
- 🗄️ MySQL 8.0
- ⚡ Redis 7.x
- ⚙️ 后端服务 (emci-new)
- 🎨 前端服务 (emci-vue3)

---

## ⚙️ 配置详解

### 后端配置 (application.yml)

```yaml
# 完整配置示例
server:
  port: 8087
  servlet:
    context-path: /api

spring:
  application:
    name: emci-new

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/emci?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 6000ms
      lettuce:
        pool:
          max-active: 10
          max-idle: 8
          min-idle: 0

# JWT 配置
app:
  jwt:
    secret: your-256-bit-secret-key-here-must-be-at-least-32-characters
    expiration: 24
    header: Authorization
    prefix: Bearer

# 文件上传配置
file:
  upload:
    path: ./uploads
    max-size: 10MB

# 日志配置
logging:
  level:
    com.alian.emci: DEBUG
    org.springframework.security: DEBUG
```

### 前端配置 (vite.config.ts)

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8087',  # 后端地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})
```

---

## 📖 用户指南

### 🔐 登录与注册

**默认测试账号：**

| 角色 | 用户名 | 密码 | 功能权限 |
|------|--------|------|---------|
| 👨‍💼 管理员 | `admin` | `123456` | 所有功能 |
| 🔍 检测员 | `inspector` | `123456` | 检测、井盖管理 |
| 🔧 维修员 | `repairer` | `123456` | 维修任务 |

### 📊 数据大屏使用

1. **进入方式**：侧边栏菜单 → 数据大屏
2. **功能说明**：
   - 实时显示井盖统计数据
   - 自动刷新（每30秒）
   - 支持全屏展示（按 F11）

### 🔍 AI 检测操作步骤

**单张检测：**
1. 进入「AI 智能检测」页面
2. 点击上传区域选择图片
3. 系统自动识别并显示结果
4. 查看缺陷位置和置信度

**批量检测：**
1. 选择「批量检测」模式
2. 上传包含多张图片的 ZIP 文件
3. 等待批量处理完成
4. 下载检测报告

### 🗺️ 地图使用指南

| 操作 | 说明 |
|------|------|
| 🔍 缩放 | 鼠标滚轮或 +/- 按钮 |
| 🖱️ 平移 | 按住鼠标左键拖动 |
| 👆 点击 | 查看井盖详情 |
| 🎨 筛选 | 顶部按钮筛选状态 |
| 🚁 巡检 | 选择无人机，设置半径，规划路径 |

### 🔧 维修工单流程

```
检测发现缺陷 → 自动生成工单 → 分配维修员 → 现场维修 → 提交完成 → 审核确认
```

**维修员操作：**
1. 登录后进入「维修管理」
2. 查看「待处理」任务列表
3. 点击「处理」开始维修
4. 上传维修后照片
5. 点击「完成」提交审核

### 🚁 无人机巡检

1. **部署无人机**：
   - 进入「无人机管理」
   - 选择闲置状态的无人机
   - 在地图上拖拽设置位置
   - 调整巡检半径（100-2000米）
   - 点击「预览路径」查看规划路线
   - 点击「确认部署」完成设置

2. **开始巡检**：
   - 部署后点击「开始巡检」
   - 无人机状态变为「巡检中」
   - 实时显示巡检进度

---

## 🔌 API 文档

启动服务后，可通过以下地址访问 API 文档：

| 文档类型 | 访问地址 |
|---------|---------|
| 📚 Knife4j 文档 | http://localhost:8087/api/doc.html |
| 📄 OpenAPI JSON | http://localhost:8087/api/v3/api-docs |
| 🌐 Swagger UI | http://localhost:8087/api/swagger-ui.html |

### 主要接口模块

#### 🔐 认证模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 用户登录获取 Token |
| 注册 | POST | `/api/auth/register` | 用户注册 |
| 登出 | POST | `/api/auth/logout` | 退出登录 |
| 刷新 Token | POST | `/api/auth/refresh` | 刷新访问令牌 |

#### 👤 用户模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取当前用户 | GET | `/api/user/me` | 获取登录用户信息 |
| 更新用户 | PUT | `/api/user/{id}` | 更新用户信息 |
| 修改密码 | PUT | `/api/user/password` | 修改登录密码 |
| 上传头像 | POST | `/api/user/avatar` | 上传用户头像 |

#### 🕳️ 井盖模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 井盖列表 | GET | `/api/manhole` | 分页查询井盖列表 |
| 井盖详情 | GET | `/api/manhole/{id}` | 获取井盖详情 |
| 创建井盖 | POST | `/api/manhole` | 创建新井盖 |
| 更新井盖 | PUT | `/api/manhole/{id}` | 更新井盖信息 |
| 删除井盖 | DELETE | `/api/manhole/{id}` | 删除井盖 |
| 附近井盖 | GET | `/api/manhole/nearby` | 获取附近井盖 |

#### 🔍 检测模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 单张检测 | POST | `/api/detection/single` | 单张图片检测 |
| 批量检测 | POST | `/api/detection/batch` | 批量图片检测 |
| 检测记录 | GET | `/api/detection/records` | 获取检测记录 |
| 检测详情 | GET | `/api/detection/{id}` | 获取检测详情 |

#### 🔧 维修模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 工单列表 | GET | `/api/repair` | 获取维修工单列表 |
| 创建工单 | POST | `/api/repair` | 创建维修工单 |
| 更新进度 | PUT | `/api/repair/{id}/status` | 更新维修进度 |
| 完成工单 | PUT | `/api/repair/{id}/complete` | 提交完成 |

#### 📊 统计模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 仪表盘统计 | GET | `/api/dashboard/stats` | 获取数据大屏统计 |

---

## 📁 项目结构

```
emic/
├── 📂 emci-new/                          # 🖥️ 后端项目
│   ├── 📂 src/main/java/com/alian/emci/
│   │   ├── 📂 config/                    # 配置类
│   │   │   ├── SecurityConfig.java       # 安全配置
│   │   │   ├── RedisConfig.java          # Redis配置
│   │   │   └── WebConfig.java            # Web配置
│   │   ├── 📂 controller/                # 控制器层
│   │   │   ├── AuthController.java       # 认证接口
│   │   │   ├── ManholeController.java    # 井盖接口
│   │   │   ├── DetectionController.java  # 检测接口
│   │   │   ├── DashboardController.java  # 统计接口
│   │   │   └── ...
│   │   ├── 📂 service/                   # 业务逻辑层
│   │   │   ├── impl/                     # 实现类
│   │   │   └── ...
│   │   ├── 📂 mapper/                    # 数据访问层
│   │   ├── 📂 entity/                    # 实体类
│   │   ├── 📂 dto/                       # 数据传输对象
│   │   ├── 📂 vo/                        # 视图对象
│   │   └── 📂 security/                  # 安全相关
│   ├── 📂 src/main/resources/
│   │   ├── 📂 mapper/                    # MyBatis XML
│   │   ├── 📂 sql/                       # SQL脚本
│   │   └── 📄 application.yml            # 配置文件
│   ├── 📄 yolo_detect_onnx.py            # AI检测脚本
│   └── 📄 pom.xml                        # Maven配置
│
├── 📂 emci-vue3/                         # 🎨 前端项目
│   ├── 📂 src/
│   │   ├── 📂 api/                       # API接口封装
│   │   ├── 📂 views/                     # 页面视图
│   │   │   ├── dashboard/                # 数据大屏
│   │   │   ├── home/                     # 首页
│   │   │   ├── manhole/                  # 井盖管理
│   │   │   ├── detection/                # AI检测
│   │   │   ├── map/                      # 地图
│   │   │   ├── repair/                   # 维修管理
│   │   │   └── drone/                    # 无人机
│   │   ├── 📂 components/                # 公共组件
│   │   ├── 📂 stores/                    # Pinia状态管理
│   │   ├── 📂 router/                    # 路由配置
│   │   ├── 📂 utils/                     # 工具函数
│   │   ├── 📂 types/                     # TypeScript类型
│   │   └── 📂 layouts/                   # 布局组件
│   ├── 📄 vite.config.ts                 # Vite配置
│   └── 📄 package.json                   # 依赖配置
│
├── 📄 deploy.bat                         # 🚀 Windows一键启动脚本
├── 📄 docker-compose-new.yml             # 🐳 Docker编排文件
├── 📄 emci.sql                           # 🗄️ 数据库初始化脚本
└── 📄 README.md                          # 📖 本文件
```

---

## ❓ 常见问题

### Q1: 启动时提示 "端口被占用"？

**A:** 使用 `deploy.bat` 会自动清理端口，或手动执行：
```bash
# 查找占用 8087 端口的进程
netstat -ano | findstr "8087"

# 结束进程（替换 <PID> 为实际进程号）
taskkill /F /PID <PID>
```

### Q2: 后端启动失败，提示数据库连接错误？

**A:** 检查以下几点：
1. MySQL 服务是否已启动
2. `application.yml` 中的数据库用户名/密码是否正确
3. 数据库 `emci` 是否已创建
4. 是否执行了 `emci.sql` 初始化脚本

### Q3: AI 检测功能无法使用？

**A:**
1. 确保 Python 3.10+ 已安装
2. 安装 Python 依赖：`pip install -r emci-new/requirements_onnx.txt`
3. 检查 `application.yml` 中的 Python 路径配置

### Q4: 地图不显示或显示空白？

**A:**
1. 检查网络连接（高德地图需要外网）
2. 确认高德地图 API Key 已正确配置
3. 浏览器控制台查看是否有 JS 错误

---

## 📝 更新日志

### v1.0.0 (2024-03)

- ✨ 初始版本发布
- ✨ AI 井盖缺陷检测（YOLO + ONNX）
- ✨ 数据可视化大屏
- ✨ 高德地图集成与路径规划
- ✨ 维修工单管理流程
- ✨ 无人机巡检管理
- ✨ Docker 容器化部署

---

## 📄 许可证

本项目基于 [MIT](LICENSE) 许可证开源。

```
MIT License

Copyright (c) 2024 EMCI Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

<div align="center">

**[⬆ 返回顶部](#-emci---智能井盖检测管理系统)**

Made with ❤️ by EMCI Team

⭐ Star us on GitHub — it motivates us a lot!

</div>
