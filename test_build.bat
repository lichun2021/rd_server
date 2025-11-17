@echo off
setlocal
cd /d %~dp0

echo ========================================
echo    测试 gmscript 编译
echo ========================================
echo.

echo 步骤 1: 检查 build.gradle 配置
echo.
findstr /C:"sourceSets.main.java.srcDirs" GameServer\build.gradle
echo.

echo 步骤 2: 检查 gmscript 源文件
echo.
if exist "GameServer\gmscript\com\hawk\game\gmscript\ChangePlatformHandler.java" (
    echo [✓] ChangePlatformHandler.java 存在
) else (
    echo [✗] ChangePlatformHandler.java 不存在
)
echo.

echo 步骤 3: 清理并重新编译
echo.
call gradle :GameServer:clean :GameServer:compileJava -x test --info

echo.
echo 步骤 4: 检查编译输出中的 gmscript 相关信息
echo.
findstr /C:"gmscript" compile_output.txt
echo.

echo 步骤 5: 检查编译结果
echo.
if exist "GameServer\build\classes\java\main\com\hawk\game\gmscript\ChangePlatformHandler.class" (
    echo [✓✓✓] ChangePlatformHandler.class 编译成功！
) else (
    echo [✗✗✗] ChangePlatformHandler.class 未生成
)
echo.

echo 步骤 6: 列出 build 目录中的所有 gmscript 文件
dir /s /b GameServer\build\classes\java\main\com\hawk\game\gmscript\*.class 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo     （没有找到任何 gmscript 的 .class 文件）
)
echo.

echo ========================================
echo 完整编译日志已保存到: compile_output.txt
echo 请查看该文件了解详细信息
echo ========================================
pause