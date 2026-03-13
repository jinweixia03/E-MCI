<div align="center">

<img src="https://via.placeholder.com/120x120/409EFF/FFFFFF?text=EMCI" alt="EMCI Logo" width="120" height="120">

# EMCI - 智能井盖检测管理系统

<p>
  <a href="https://spring.io/projects/spring-boot">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.2.3-6DB33F?logo=spring-boot" alt="Spring Boot">
  </a>
  <a href="https://vuejs.org/">
    <img src="https://img.shields.io/badge/Vue.js-3.4-4FC08D?logo=vue.js" alt="Vue.js">
  </a>
  <a href="https://onnx.ai/">
    <img src="https://img.shields.io/badge/ONNX-Runtime-005CED?logo=onnx" alt="ONNX">
  </a>
  <a href="https://www.docker.com/">
    <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker" alt="Docker">
  </a>
</p>

<p>
  <img src="https://img.shields.io/badge/Java-17-007396?logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/TypeScript-5.3-3178C6?logo=typescript" alt="TypeScript">
  <img src="https://img.shields.io/badge/Python-3.10-3776AB?logo=python" alt="Python">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql" alt="MySQL">
</p>

<p>
  <a href="#快速开始">快速开始</a> •
  <a href="#技术架构">技术架构</a> •
  <a href="#在线演示">在线演示</a> •
  <a href="#API文档">API文档</a> •
  <a href="#贡献指南">贡献指南</a>
</p>

<img src="https://via.placeholder.com/800x400/1a1a2e/FFFFFF?text=Dashboard+Preview" alt="Dashboard Preview" width="800">

</div>

---

## 📋 目录

- [项目简介](#-项目简介)
- [核心特性](#-核心特性)
- [技术架构](#-技术架构)
- [快速开始](#-快速开始)
  - [环境要求](#环境要求)
  - [Docker 部署（推荐）](#docker-部署推荐)
  - [本地开发](#本地开发)
- [配置指南](#-配置指南)
- [API 文档](#-api-文档)
- [项目结构](#-项目结构)
- [测试账号](#-测试账号)
- [更新日志](#-更新日志)
- [贡献者](#-贡献者)
- [许可证](#-许可证)

---

## 🎯 项目简介

**EMCI** (Smart Manhole Inspection System) 是一款基于深度学习的智能井盖检测管理系统。系统利用 YOLO 目标检测算法自动识别井盖缺陷（破损、移位、缺失等），结合 GIS 地图实现井盖资产的数字化管理和维护流程的智能化调度。

### 为什么选择 EMCI？

| 特性 | 传统方式 | EMCI |
|------|---------|------|
| 检测方式 | 人工巡检 | 🤖 AI 自动识别 |
| 数据记录 | 纸质/Excel | 📊 云端数字化管理 |
| 缺陷定位 | 描述不清 | 🗺️ GPS 精准定位 |
| 维修调度 | 电话沟通 | 🔔 系统自动派单 |
| 统计分析 | 手动汇总 | 📈 实时数据大屏 |

---

## ✨ 核心特性

### 🤖 AI 智能检测
- 基于 **YOLOv8** + **ONNX Runtime** 的高性能推理
- 支持多种缺陷类型识别：破损、裂纹、移位、缺失、异物
- 批量图片检测与实时视频流检测
- 检测结果自动标注并生成报告

### 📊 数据可视化大屏
- 集成 **ECharts** + **DataV Vue3**
- 实时检测数据统计与趋势分析
- 井盖分布热力图
- 缺陷类型占比分析
- 维修工单处理进度

### 🗺️ GIS 地图集成
- 高德地图 API 深度集成
- 井盖点位精准标注与聚合显示
- 检测轨迹路线规划
- 区域围栏与告警

### 🔐 企业级权限管理
- 基于 **Spring Security** + **JWT** 的 RBAC 模型
- 多角色支持：管理员、检测员、维修员、审核员
- 操作日志审计与数据权限隔离

### 🐳 云原生部署
- **Docker Compose** 一键部署
- 支持集群扩展与负载均衡
- 健康检查与自动重启

---

## 🏗️ 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Frontend)                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Vue 3     │  │ Element Plus│  │   DataV / ECharts   │  │
│  │  TypeScript │  │   UI 组件   │  │     数据大屏         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      网关层 (Gateway)                         │
│              Nginx 反向代理 + 静态资源托管                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       后端层 (Backend)                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              Spring Boot 3.2.3                       │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │    │
│  │  │   Web    │ │  Security│ │ Validation│ │  AOP    │ │    │
│  │  └──────────┘ └──────────┘ └──────────┘ └─────────┘ │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │    │
│  │  │MyBatisPlus│ │  Redis   │ │   JWT    │ │  COS    │ │    │
│  │  └──────────┘ └──────────┘ └──────────┘ └─────────┘ │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   MySQL 8.0     │ │   Redis 7.x     │ │  AI Detection   │
│   业务数据       │ │   缓存/会话      │ │  YOLO + ONNX    │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

### 技术栈详情

#### 后端 (emci-new)

| 技术 | 版本 | 用途 |
|------|------|------|
| [Spring Boot](https://spring.io/projects/spring-boot) | 3.2.3 | 核心框架 |
| [MyBatis Plus](https://baomidou.com/) | 3.5.5 | ORM 框架 |
| [Spring Security](https://spring.io/projects/spring-security) | 6.x | 安全认证 |
| [JWT](https://github.com/auth0/java-jwt) | 4.4.0 | Token 认证 |
| [Redis](https://redis.io/) | 7.x | 缓存/会话 |
| [Knife4j](https://doc.xiaominfo.com/) | 4.4.0 | API 文档 |
| [Hutool](https://hutool.cn/) | 5.8.23 | 工具库 |
| [Tencent COS](https://cloud.tencent.com/product/cos) | 5.6.x | 对象存储 |

#### 前端 (emci-vue3)

| 技术 | 版本 | 用途 |
|------|------|------|
| [Vue](https://vuejs.org/) | 3.4.19 | 前端框架 |
| [TypeScript](https://www.typescriptlang.org/) | 5.3.3 | 类型系统 |
| [Vite](https://vitejs.dev/) | 5.1.x | 构建工具 |
| [Element Plus](https://element-plus.org/) | 2.5.6 | UI 组件库 |
| [Pinia](https://pinia.vuejs.org/) | 2.1.7 | 状态管理 |
| [Vue Router](https://router.vuejs.org/) | 4.3.0 | 路由管理 |
| [ECharts](https://echarts.apache.org/) | 5.5.0 | 图表库 |
| [DataV Vue3](https://datav-vue3.netlify.app/) | 1.7.4 | 数据大屏 |

#### AI 检测

| 技术 | 用途 |
|------|------|
| [YOLO](https://github.com/ultralytics/ultralytics) | 目标检测算法 |
| [ONNX Runtime](https://onnxruntime.ai/) | 跨平台推理引擎 |
| [OpenCV](https://opencv.org/) | 图像处理 |

---

## 🚀 快速开始

### 环境要求

| 依赖 | 版本 | 下载 |
|------|------|------|
| JDK | 17+ | [下载](https://adoptium.net/) |
| Node.js | 18+ | [下载](https://nodejs.org/) |
| MySQL | 8.0+ | [下载](https://dev.mysql.com/) |
| Redis | 7+ | [下载](https://redis.io/download/) |
| Python | 3.10+ | [下载](https://www.python.org/) |
| Docker | 最新版 | [下载](https://www.docker.com/) |

### Docker 部署（推荐）

```bash
# 1. 克隆仓库
git clone <repository-url>
cd emic

# 2. 启动所有服务
docker-compose -f docker-compose-new.yml up -d

# 3. 查看服务状态
docker-compose -f docker-compose-new.yml ps

# 4. 访问系统
# 前端: http://localhost
# 后端 API: http://localhost:8085
# API 文档: http://localhost:8085/api/doc.html
```

### 本地开发

#### 1. 数据库初始化

```bash
# 登录 MySQL 并创建数据库
mysql -u root -p

# 执行 SQL 脚本
source emci.sql
```

#### 2. 后端启动

```bash
cd emci-new

# Maven 方式
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package -DskipTests
java -jar target/emci-new-1.0.0.jar
```

服务地址: http://localhost:8086/api

#### 3. 前端启动

```bash
cd emci-vue3

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

开发服务器: http://localhost:5173

---

## ⚙️ 配置指南

### 后端配置

编辑 `emci-new/src/main/resources/application.yml`:

```yaml
server:
  port: 8086

spring:
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/emci?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

  # Redis 配置
  data:
    redis:
      host: localhost
      port: 6379

# JWT 配置
app:
  jwt:
    secret: your-256-bit-secret-key-here
    expiration: 24  # 小时

# YOLO 检测配置
yolo:
  model:
    path: models/best.onnx
  python:
    path: python3
  script:
    path: yolo_detect_onnx.py
```

### 前端配置

编辑 `emci-vue3/vite.config.ts`:

```typescript
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8086',
        changeOrigin: true
      }
    }
  }
})
```

---

## 📚 API 文档

启动服务后访问以下地址：

| 文档类型 | 地址 |
|---------|------|
| Knife4j 文档 | http://localhost:8086/api/doc.html |
| OpenAPI JSON | http://localhost:8086/api/v3/api-docs |
| Swagger UI | http://localhost:8086/api/swagger-ui.html |

### 主要接口模块

- 🔐 **认证模块** - 登录、登出、Token 刷新
- 👤 **用户模块** - 用户管理、角色权限
- 🔧 **井盖模块** - 井盖 CRUD、批量导入导出
- 🔍 **检测模块** - AI 检测、检测记录、缺陷分析
- 🛠️ **维修模块** - 工单管理、维修进度跟踪
- 📊 **统计模块** - 数据大屏、报表导出

---

## 📁 项目结构

```
emic/
├── 📂 emci-new/                    # 后端项目
│   ├── 📂 src/main/java/com/alian/emci/
│   │   ├── 📂 config/              # 配置类
│   │   ├── 📂 controller/          # 控制器层
│   │   ├── 📂 service/             # 业务逻辑层
│   │   ├── 📂 mapper/              # 数据访问层
│   │   ├── 📂 entity/              # 实体类
│   │   ├── 📂 dto/                 # DTO
│   │   ├── 📂 vo/                  # VO
│   │   ├── 📂 utils/               # 工具类
│   │   └── 📂 security/            # 安全配置
│   ├── 📂 src/main/resources/
│   │   ├── 📂 mapper/              # MyBatis XML
│   │   ├── 📂 sql/                 # 初始化脚本
│   │   └── 📄 application.yml      # 配置文件
│   ├── 📄 yolo_detect_onnx.py      # AI 检测脚本
│   ├── 📄 requirements_onnx.txt    # Python 依赖
│   └── 📄 Dockerfile
│
├── 📂 emci-vue3/                   # 前端项目
│   ├── 📂 src/
│   │   ├── 📂 api/                 # API 接口
│   │   ├── 📂 views/               # 页面视图
│   │   ├── 📂 components/          # 公共组件
│   │   ├── 📂 stores/              # Pinia 状态
│   │   ├── 📂 router/              # 路由配置
│   │   ├── 📂 utils/               # 工具函数
│   │   ├── 📂 types/               # TS 类型
│   │   ├── 📂 styles/              # 样式文件
│   │   ├── 📂 layouts/             # 布局组件
│   │   └── 📂 composables/         # 组合式函数
│   ├── 📄 vite.config.ts
│   └── 📄 Dockerfile
│
├── 📄 docker-compose-new.yml       # Docker 编排
├── 📄 emci.sql                     # 数据库脚本
└── 📄 README.md                    # 本文件
```

---

## 👤 测试账号

系统初始化后可用以下账号登录：

| 角色 | 账号 | 密码 | 权限 |
|------|------|------|------|
| 超级管理员 | `admin` | `123456` | 全部权限 |
| 检测员 | `inspector` | `123456` | 检测相关 |
| 维修员 | `repairer` | `123456` | 维修工单 |

---

## 📝 更新日志

### v1.0.0 (2024-03)

- ✨ AI 井盖缺陷检测（YOLO + ONNX）
- ✨ 数据可视化大屏
- ✨ 高德地图集成
- ✨ 维修工单管理
- ✨ Docker 容器化部署

---

## 👥 贡献者

感谢所有为项目做出贡献的开发者！

<a href="https://github.com/yourusername/emci/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=yourusername/emci" alt="Contributors" />
</a>

### 如何贡献

1. **Fork** 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add: 新功能描述'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 创建 **Pull Request**

请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细规范。

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

## 🙏 致谢

- [Ultralytics YOLO](https://github.com/ultralytics/ultralytics) - 优秀的目标检测框架
- [Element Plus](https://element-plus.org/) - 精美的 Vue 3 组件库
- [DataV Vue3](https://datav-vue3.netlify.app/) - 数据可视化组件
- [Spring Boot](https://spring.io/projects/spring-boot) - Java 生态最佳实践

---

<div align="center">

**[⬆ 返回顶部](#emci---智能井盖检测管理系统)**

Made with ❤️ by EMCI Team

</div>
