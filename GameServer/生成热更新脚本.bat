@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
cd /d %~dp0

echo ====================================
echo    生成热更新文件脚本
echo ====================================
echo.

rem 获取日期时间（使用 PowerShell 确保格式正确）
for /f "delims=" %%a in ('powershell -Command "Get-Date -Format 'yyyyMMdd_HHmm'"') do set DATE_STR=%%a

rem 设置路径
set NEW_CLASSES=build\classes\java\main
set OLD_CLASSES=online\classes\java\main
set ONLINE_DIR=online
set HOTFIX_DIR=%ONLINE_DIR%\hotfix_%DATE_STR%
set OUTPUT_BAT=%ONLINE_DIR%\hotfix_%DATE_STR%.bat

echo 日期: %DATE_STR%
echo.

rem 检查目录是否存在
if not exist "%NEW_CLASSES%" (
    echo 错误: 找不到新编译的 classes 目录: %NEW_CLASSES%
    echo 请先运行编译脚本！
    pause
    exit /b 1
)

if not exist "%OLD_CLASSES%" (
    echo 错误: 找不到线上 classes 目录: %OLD_CLASSES%
    echo 请确保 online 目录存在！
    pause
    exit /b 1
)

rem 清空并创建 hotfix 目录
if exist "%HOTFIX_DIR%" (
    echo 清空旧的 hotfix 目录...
    rd /s /q "%HOTFIX_DIR%" 2>nul
)
mkdir "%HOTFIX_DIR%" 2>nul

echo 开始对比文件差异...
echo.

rem 获取 NEW_CLASSES 的绝对路径
pushd "%NEW_CLASSES%"
set NEW_CLASSES_FULL=%CD%
popd

rem 创建临时文件存储类名
set TEMP_CLASSES=%temp%\hotfix_classes_%DATE_STR%_%RANDOM%.txt
if exist "%TEMP_CLASSES%" del "%TEMP_CLASSES%"

rem 统计计数
set /a NEW_COUNT=0
set /a MODIFIED_COUNT=0
set /a TOTAL_COUNT=0

rem 遍历新编译的所有文件（不只是 class 文件）
for /r "%NEW_CLASSES%" %%F in (*.*) do (
    set "NEW_FILE=%%F"
    set "REL_PATH=!NEW_FILE:%NEW_CLASSES_FULL%\=!"
    set "OLD_FILE=%OLD_CLASSES%\!REL_PATH!"
    
    set CHANGED=0
    
    rem 检查文件是否存在于 online
    if not exist "!OLD_FILE!" (
        echo [新增] !REL_PATH!
        set CHANGED=1
        set /a NEW_COUNT+=1
    ) else (
        rem 对比文件内容
        fc /b "%%F" "!OLD_FILE!" >nul 2>&1
        if errorlevel 1 (
            echo [修改] !REL_PATH!
            set CHANGED=1
            set /a MODIFIED_COUNT+=1
        )
    )
    
    rem 如果文件有变化，复制到 hotfix 目录
    if !CHANGED! equ 1 (
        set /a TOTAL_COUNT+=1
        
        rem 创建目标目录（保留 classes/java/main 路径）
        set "TARGET_FILE=%HOTFIX_DIR%\classes\java\main\!REL_PATH!"
        for %%D in ("!TARGET_FILE!") do set "TARGET_DIR=%%~dpD"
        if not exist "!TARGET_DIR!" mkdir "!TARGET_DIR!" 2>nul
        
        rem 复制文件
        copy /y "%%F" "!TARGET_FILE!" >nul
        
        rem 如果是 class 文件，提取类名（排除内部类）
        echo %%~xF | findstr /i ".class" >nul
        if !errorlevel! equ 0 (
            set "CLASS_NAME=!REL_PATH:\=.!"
            set "CLASS_NAME=!CLASS_NAME:.class=!"
            
            rem 检查是否是内部类（包含 $ 符号）
            echo !CLASS_NAME! | findstr /C:"$" >nul
            if errorlevel 1 (
                rem 不是内部类，记录到临时文件
                echo !CLASS_NAME!>>"%TEMP_CLASSES%"
            )
        )
    )
)

echo.
echo ====================================
echo 差异统计:
echo   新增文件: %NEW_COUNT%
echo   修改文件: %MODIFIED_COUNT%
echo   总计文件: %TOTAL_COUNT%
echo ====================================
echo.

if %TOTAL_COUNT% equ 0 (
    echo 没有发现差异文件，无需生成热更新脚本
    if exist "%TEMP_CLASSES%" del "%TEMP_CLASSES%"
    pause
    exit /b 0
)

rem 统计主类数量
set /a CLASS_COUNT=0
if exist "%TEMP_CLASSES%" (
    for /f %%i in ('type "%TEMP_CLASSES%" 2^>nul ^| find /c /v ""') do set CLASS_COUNT=%%i
)

echo 主类数量: %CLASS_COUNT%
echo.

rem 生成批处理文件
echo 正在生成热更新脚本: %OUTPUT_BAT%
echo.

rem 使用临时文件避免编码问题
set TEMP_BAT=%temp%\hotfix_temp_%DATE_STR%_%RANDOM%.bat

rem 创建批处理文件头部
(
echo @echo off
echo setlocal
echo cd /d %%~dp0
echo.
echo echo ====================================
echo echo    批量热更新脚本
echo echo    生成时间: %date% %time%
echo echo    差异类数: %CLASS_COUNT%
echo echo ====================================
echo echo.
echo.
echo rem 检查端口号参数
echo if "%%~1"=="" ^(
echo     echo 用法: hotfix_%DATE_STR%.bat [端口号]
echo     echo 示例: hotfix_%DATE_STR%.bat 8080
echo     echo.
echo     pause
echo     exit /b 1
echo ^)
echo.
echo set PORT=%%~1
echo echo 使用端口: %%PORT%%
echo echo.
echo.
) > "%TEMP_BAT%"

rem 添加每个类的热更新命令
if exist "%TEMP_CLASSES%" (
    for /f "usebackq delims=" %%C in ("%TEMP_CLASSES%") do (
        echo echo [热更新] %%C >> "%TEMP_BAT%"
        echo call .\hotfix-class.bat %%C %%PORT%% >> "%TEMP_BAT%"
        echo timeout /t 1 /nobreak ^>nul >> "%TEMP_BAT%"
        echo echo. >> "%TEMP_BAT%"
    )
)

rem 添加结束部分
(
echo.
echo echo ====================================
echo echo 热更新完成！
echo echo ====================================
echo pause
) >> "%TEMP_BAT%"

rem 复制到目标位置（使用 copy 保持编码）
copy /y "%TEMP_BAT%" "%OUTPUT_BAT%" >nul
del "%TEMP_BAT%" 2>nul

rem 删除临时文件
if exist "%TEMP_CLASSES%" del "%TEMP_CLASSES%"

echo.
echo ====================================
echo 处理完成！
echo ====================================
echo 差异文件数: %TOTAL_COUNT%
echo 已复制到: %HOTFIX_DIR%
echo 生成脚本: %OUTPUT_BAT%
echo.
echo 使用方法:
echo   cd online
echo   .\hotfix_%DATE_STR%.bat 8080
echo.
pause
exit /b 0
