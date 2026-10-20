@echo off
REM =====================================================
REM 本地开发环境一键启动（Windows）
REM =====================================================
REM 用法：
REM   start-dev.bat          仅启动前端
REM   start-dev.bat all      同时启动后端 + 前端
REM   start-dev.bat backend  仅启动后端
REM   start-dev.bat frontend 仅启动前端
REM
REM 访问地址：http://localhost:4321
REM 管理员：admin / admin123
REM =====================================================
setlocal enabledelayedexpansion
chcp 65001 >nul

set "ROOT=%~dp0"
set "BACKEND=%ROOT%blog-backend"
set "FRONTEND=%ROOT%social-factory-blog"
set "LOGDIR=%ROOT%.dev-logs"

if not exist "%LOGDIR%" mkdir "%LOGDIR%"

REM ---------- 端口检查 ----------
call :checkPort 8080 "后端"
if "%PORT_BUSY%"=="1" (
    echo [WARN] 8080 已被占用，跳过后端启动
    goto :frontend
)

echo [INFO] 启动后端 (Spring Boot, 端口 8080)...
cd /d "%BACKEND%"
start "blog-backend" cmd /c "mvn spring-boot:run > \"%LOGDIR%\backend.log\" 2>&1"
echo [INFO] 后端启动中，日志：%LOGDIR%\backend.log
echo [INFO] 等待数据库连接与 Spring 初始化（约 20-40 秒）...

set /a WAITED=0
:waitBackend
timeout /t 3 /nobreak >nul
set /a WAITED+=3
curl -s -m 3 -o nul http://localhost:8080/api/captcha/generate 2>nul
if not errorlevel 1 (
    echo [OK] 后端就绪 -^> http://localhost:8080/api
    goto :frontend
)
if !WAITED! GEQ 90 (
    echo [WARN] 后端等待超时，请查看 %LOGDIR%\backend.log
    goto :frontend
)
goto :waitBackend

:frontend
call :checkPort 4321 "前端"
if "%PORT_BUSY%"=="1" (
    echo [INFO] 前端已在运行，跳过
    goto :done
)

echo [INFO] 启动前端 (Astro dev, 端口 4321)...
cd /d "%FRONTEND%"
set "SITE_URL=http://localhost:4321"
start "blog-frontend" cmd /c "npm run dev > \"%LOGDIR%\frontend.log\" 2>&1"
echo [INFO] 前端启动中，日志：%LOGDIR%\frontend.log

set /a WAITED=0
:waitFrontend
timeout /t 2 /nobreak >nul
set /a WAITED+=2
curl -s -m 3 -o nul http://localhost:4321/ 2>nul
if not errorlevel 1 (
    echo [OK] 前端就绪 -^> http://localhost:4321
    goto :done
)
if !WAITED! GEQ 60 (
    echo [WARN] 前端等待超时，请查看 %LOGDIR%\frontend.log
    goto :done
)
goto :waitFrontend

:done
echo.
echo [INFO] 常用地址：
echo   前端页面   http://localhost:4321
echo   注册页     http://localhost:4321/register
echo   后端接口   http://localhost:8080/api
echo   管理员     admin / admin123
echo   日志目录   %LOGDIR%
echo.
pause
exit /b 0

:checkPort
netstat -ano | findstr /R /C:":%1 .*LISTENING" >nul
if errorlevel 1 (
    set "PORT_BUSY=0"
) else (
    set "PORT_BUSY=1"
    echo [OK] %2 端口 %1 已在监听
)
exit /b 0
