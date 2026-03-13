@echo off
chcp 65001 >nul
echo ============================================
echo EMCI 智能井盖检测系统 - 快速启动脚本
echo ============================================
echo.

REM 设置变量
set "BACKEND_DIR=D:\1_Source\XFCode\emic\emci-new"
set "FRONTEND_DIR=D:\1_Source\XFCode\emic\emci-vue3"
set "MAVEN_CMD=D:\Environments\maven\apache-maven-3.9.10\bin\mvn.cmd"
set "BACKEND_PORT=8086"
set "FRONTEND_PORT=5173"

REM ========== 检查环境 ==========
echo [检查环境...]

java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [错误] 未找到 Java
    pause
    exit /b 1
)
echo [✓] Java

if not exist "%MAVEN_CMD%" (
    echo [错误] 未找到 Maven: %MAVEN_CMD%
    pause
    exit /b 1
)
echo [✓] Maven

node --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [错误] 未找到 Node.js
    pause
    exit /b 1
)
echo [✓] Node.js

REM ========== 关闭旧服务 ==========
echo.
echo [关闭旧服务...]

REM 关闭标题为 EMCI* 的窗口
taskkill /F /FI "WINDOWTITLE eq EMCI*" >nul 2>&1

REM 关闭 Java 和 Node 进程
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
taskkill /F /IM node.exe >nul 2>&1

REM 释放端口 - 后端
echo [检查端口 %BACKEND_PORT%]...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT%" ^| findstr "LISTENING" 2^>nul') do (
    echo [释放端口 %BACKEND_PORT%] 终止进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

REM 释放端口 - 前端
echo [检查端口 %FRONTEND_PORT%]...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%FRONTEND_PORT%" ^| findstr "LISTENING" 2^>nul') do (
    echo [释放端口 %FRONTEND_PORT%] 终止进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

REM 简单等待 3 秒
timeout /t 3 /nobreak >nul
echo [✓] 旧服务已清理

REM ========== 启动后端 ==========
echo.
echo ============================================
echo [1/2] 启动后端服务
echo ============================================
echo.

cd /d "%BACKEND_DIR%"

REM 检查是否已有编译好的 target 目录
if exist "target\classes" (
    echo [检测到已编译的代码，直接启动]
) else (
    echo [首次运行，需要编译...]
    echo [编译中，请等待约1-3分钟...]
    call "%MAVEN_CMD%" compile -q -DskipTests
    if %ERRORLEVEL% neq 0 (
        echo [错误] 编译失败
        pause
        exit /b 1
    )
    echo [✓] 编译完成
)

echo [启动后端...]
start "EMCI 后端服务" cmd /c "cd /d "%BACKEND_DIR%" && "%MAVEN_CMD%" spring-boot:run -DskipTests"

echo [后端启动中，请等待...]
timeout /t 8 /nobreak >nul
echo [✓] 后端已启动

REM ========== 启动前端 ==========
echo.
echo ============================================
echo [2/2] 启动前端服务
echo ============================================
echo.

cd /d "%FRONTEND_DIR%"

if not exist "node_modules" (
    echo [安装前端依赖，请等待...]
    call npm install
    if %ERRORLEVEL% neq 0 (
        echo [错误] npm install 失败
        pause
        exit /b 1
    )
)

echo [启动前端...]
start "EMCI 前端服务" cmd /c "cd /d "%FRONTEND_DIR%" && npm run dev"

echo [前端启动中，请等待...]
timeout /t 5 /nobreak >nul
echo [✓] 前端已启动

REM ========== 完成 ==========
echo.
echo ============================================
echo [✓] 服务已启动！
echo ============================================
echo.
echo 访问地址:
echo   前端: http://localhost:%FRONTEND_PORT%
echo   后端: http://localhost:%BACKEND_PORT%/api
echo   文档: http://localhost:%BACKEND_PORT%/api/doc.html
echo.
echo 账号: admin / 123456
echo.
echo [提示] 按任意键停止所有服务
echo.

pause >nul

REM ========== 停止服务 ==========
echo.
echo [正在停止服务...]

taskkill /F /FI "WINDOWTITLE eq EMCI*" >nul 2>&1
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
taskkill /F /IM node.exe >nul 2>&1

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT%" ^| findstr "LISTENING" 2^>nul') do (
    taskkill /F /PID %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%FRONTEND_PORT%" ^| findstr "LISTENING" 2^>nul') do (
    taskkill /F /PID %%a >nul 2>&1
)

echo [✓] 服务已停止
timeout /t 2 /nobreak >nul
