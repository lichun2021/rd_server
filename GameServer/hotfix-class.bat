@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title 热更新工具
cd /d %~dp0

echo ====================================
echo    游戏服务器热更新工具
echo ====================================
echo 当前工作目录: %CD%
echo.

rem 检查参数
if "%~1"=="" (
    echo 用法: hotfix-class.bat 类的完整路径 [端口号]
    echo 示例: hotfix-class.bat com.hawk.game.module.PlayerRechargeModule
    echo       hotfix-class.bat com.hawk.game.module.PlayerRechargeModule 8081
    echo.
    echo 或直接将 .class 文件拖到此脚本上
    pause
    exit /b 1
)

rem 设置端口号（默认 8080）
set HOTFIX_PORT=8080
if not "%~2"=="" (
    set HOTFIX_PORT=%~2
)

rem 判断是文件路径还是类名
set INPUT=%~1
echo 输入: %INPUT%

rem 如果是 .class 文件路径
if /I "%INPUT:~-6%"==".class" (
    echo 检测到 .class 文件路径
    
    rem 查找 com\hawk\game 在路径中的位置
    set CLASSFILE=%INPUT%
    
    rem 尝试从 build\classes\java\main 提取
    echo %CLASSFILE% | findstr /C:"build\classes\java\main" >nul
    if !errorlevel!==0 (
        for /f "tokens=2 delims=main\" %%a in ("%CLASSFILE%") do set RELPATH=%%a
        goto :copy_file
    )
    
    rem 尝试从 classes\java\main 提取
    echo %CLASSFILE% | findstr /C:"classes\java\main" >nul
    if !errorlevel!==0 (
        for /f "tokens=2 delims=main\" %%a in ("%CLASSFILE%") do set RELPATH=%%a
        goto :copy_file
    )
    
    echo 无法识别的 class 文件路径格式
    pause
    exit /b 1
)

rem 如果是类名格式 (com.hawk.game.module.PlayerRechargeModule)
echo 类名: %INPUT%
set CLASSNAME=%INPUT:.=\%
set RELPATH=%CLASSNAME%.class

rem 多个可能的查找路径
set CLASSFILE=
set SEARCHPATHS=build\classes\java\main;classes\java\main;D:\game\classes\java\main;..\classes\java\main

for %%P in (%SEARCHPATHS%) do (
    if exist "%%P\%RELPATH%" (
        set CLASSFILE=%%P\%RELPATH%
        set BASEPATH=%%P
        goto :found_class
    )
)

echo 错误: 找不到编译后的 class 文件
echo 查找路径1: build\classes\java\main\%RELPATH%
echo 查找路径2: classes\java\main\%RELPATH%
echo 查找路径3: D:\game\classes\java\main\%RELPATH%
echo 查找路径4: ..\classes\java\main\%RELPATH%
echo.
echo 请先编译项目，或检查 class 文件路径！
pause
exit /b 1

:found_class

:copy_file
echo.
echo 源文件: %CLASSFILE%
echo 目标路径: hotfix\%RELPATH%
echo.

rem 创建目标目录
for %%F in ("hotfix\%RELPATH%") do set TARGETDIR=%%~dpF
if not exist "%TARGETDIR%" (
    echo 创建目录: %TARGETDIR%
    mkdir "%TARGETDIR%"
)

rem 获取类名（不含路径和扩展名）
for %%F in ("%CLASSFILE%") do (
    set CLASSDIR=%%~dpF
    set CLASSBASE=%%~nF
)

rem 复制主 class 文件
echo 复制: %CLASSBASE%.class
copy /Y "%CLASSFILE%" "hotfix\%RELPATH%" >nul
if errorlevel 1 (
    echo 复制失败！
    pause
    exit /b 1
)

rem 复制内部类文件 (如 ClassName$1.class, ClassName$2.class 等)
set INNERCOUNT=0
for %%I in ("%CLASSDIR%%CLASSBASE%$*.class") do (
    set /a INNERCOUNT+=1
    for %%F in ("%%I") do (
        set INNERNAME=%%~nxF
        echo 复制内部类: !INNERNAME!
        copy /Y "%%I" "%TARGETDIR%!INNERNAME!" >nul
    )
)

if %INNERCOUNT% GTR 0 (
    echo [成功] 已复制 1 个主类 + %INNERCOUNT% 个内部类
) else (
    echo [成功] 已复制 class 文件到 hotfix 目录
)
echo.

rem 调用热更新接口
echo ====================================
echo 正在触发热更新...
echo ====================================
echo 接口地址: http://localhost:%HOTFIX_PORT%/script/hotfix
echo.

rem 直接使用正确的接口路径
curl http://localhost:%HOTFIX_PORT%/script/hotfix
echo.
echo.

echo ====================================
echo 热更新完成！
echo ====================================
pause

