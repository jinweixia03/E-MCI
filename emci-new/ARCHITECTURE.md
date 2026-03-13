# 井盖检测系统 - 优化后的架构文档

## 概述

本文档描述了井盖检测系统的优化架构，包括Python检测模块和Java后端服务的解耦合设计。

## Python检测模块

### 目录结构

```
detector/
├── __init__.py              # 模块入口
├── core/                    # 核心模块
│   ├── __init__.py
│   ├── interface.py         # 检测后端接口
│   ├── detector.py          # 主检测器类
│   └── result.py            # 结果数据模型
├── backends/                # 后端实现
│   ├── __init__.py
│   ├── base.py              # 基础类和常量
│   ├── onnx_backend.py      # ONNX Runtime后端
│   └── ultralytics_backend.py  # Ultralytics后端
└── utils/                   # 工具函数
    ├── __init__.py
    ├── image_utils.py       # 图片处理工具
    └── cli_utils.py         # 命令行工具

detect.py                    # 统一检测入口脚本
```

### 核心组件

#### 1. DetectionBackend (interface.py)
抽象基类，定义所有检测后端必须实现的接口：
- `load_model()`: 加载模型
- `detect(image)`: 执行检测
- `preprocess(image)`: 预处理
- `postprocess(outputs, ...)`: 后处理

#### 2. ManholeDetector (detector.py)
主检测器类，提供统一接口：
- `detect_image()`: 检测单张图片
- `detect_batch()`: 批量检测
- `detect_video()`: 检测视频

#### 3. 后端实现
- **ONNXBackend**: 使用ONNX Runtime，无需conda
- **UltralyticsBackend**: 使用ultralytics库，支持原生YOLOv8

### 使用方法

```bash
# 使用ONNX后端（默认）
python detect.py --source image.jpg

# 使用Ultralytics后端
python detect.py --source image.jpg --backend ultralytics

# 自定义参数
python detect.py --source image.jpg --conf 0.7 --iou 0.5
```

## Java后端服务

### 目录结构

```
src/main/java/com/alian/emci/detection/
├── package-info.java
├── core/                    # 核心接口和枚举
│   ├── DetectionBackend.java    # 检测后端接口
│   ├── DetectionContext.java    # 检测上下文
│   ├── BackendType.java         # 后端类型枚举
│   └── package-info.java
├── backend/                 # 后端实现
│   ├── AbstractDetectionBackend.java  # 抽象基类
│   ├── PythonScriptBackend.java       # Python脚本后端
│   ├── MockDetectionBackend.java      # 模拟后端
│   ├── BackendFactory.java            # 后端工厂
│   └── package-info.java
├── command/                 # 命令执行
│   ├── CommandBuilder.java      # 命令构建器
│   ├── CommandExecutor.java     # 命令执行器
│   ├── ExecutionResult.java     # 执行结果
│   └── package-info.java
└── parser/                  # 结果解析
    ├── ResultParser.java          # 结果解析器
    └── package-info.java
```

### 核心组件

#### 1. DetectionBackend 接口
defines the contract for all detection backends:
- `detect(imagePath, conf, iou)`: 检测图片
- `detectVideo(videoPath, conf, iou)`: 检测视频
- `isAvailable()`: 检查后端是否可用
- `getBackendInfo()`: 获取后端信息

#### 2. 后端实现
- **PythonScriptBackend**: 调用Python脚本
- **MockDetectionBackend**: 模拟检测，用于测试

#### 3. BackendFactory
工厂模式，负责创建和管理检测后端：
- `getBackend(type)`: 获取指定类型的后端
- `getDefaultBackend()`: 获取默认后端（自动选择）

#### 4. 辅助组件
- **CommandBuilder**: 构建Python命令
- **CommandExecutor**: 执行命令并处理超时
- **ResultParser**: 解析Python输出

### 服务层

#### YoloDetectionServiceImplV2
重构后的服务实现：
- 使用 `BackendFactory` 获取后端
- 处理文件上传和临时文件管理
- 保存结果到存储

### 使用方法

```java
// 注入服务
@Autowired
private YoloDetectionService yoloDetectionService;

// 检测图片
DetectionResultVO result = yoloDetectionService.detectImage(
    file, 0.7, 0.5, manholeId
);

// 获取模型信息
String info = yoloDetectionService.getModelInfo();
```

## 配置

### application.yml 示例

```yaml
yolo:
  use-mock: false                          # 是否使用模拟模式
  model:
    path: "models/best.onnx"              # 模型路径
  conda:
    enabled: false                        # 是否使用conda
    env-name: "yolo"
    path: "conda"
    use-conda-run: true
  python:
    path: "python"                        # Python路径
  script:
    path: "detect.py"                     # 检测脚本路径
```

## 优势

### 解耦合
1. **检测后端分离**: 可以轻松添加新的检测后端
2. **命令执行分离**: 独立的命令构建和执行模块
3. **结果解析分离**: 统一的解析器处理不同后端输出

### 可扩展性
1. 新增后端只需实现 `DetectionBackend` 接口
2. 支持多种Python后端（ONNX、Ultralytics）
3. 模拟后端便于开发和测试

### 可维护性
1. 清晰的模块划分
2. 统一的接口定义
3. 完善的包文档

## 迁移指南

### 从旧版本迁移

1. **更新配置**: 将 `yolo.script.path` 改为 `detect.py`
2. **保留旧脚本**: 旧的 `yolo_detect.py` 和 `yolo_detect_onnx.py` 仍可继续使用
3. **新服务实现**: 系统会自动使用 `YoloDetectionServiceImplV2`（带有 `@Primary` 注解）

### 兼容性

- 所有现有API保持不变
- 检测结果格式不变
- 配置文件格式向后兼容
