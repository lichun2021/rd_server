chcp 65001
cd /d %~dp0

REM 需要已安装 JDK（确保 javac 可用）。此脚本仅用 javac 编译 GameServer 源码，不依赖 Maven。

set SOURCE_DIR=GameServer\src\main\java
set OUTPUT_DIR=GameServer\target\classes
set SOURCES_LIST=GameServer\target\gs_sources.txt

REM 运行期依赖：使用现有 gameserver.jar 以及 lib 目录中的依赖作为编译时 classpath
set CP=gameserver.jar;lib\*

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
if not exist GameServer\target mkdir GameServer\target

dir /s /b "%SOURCE_DIR%\*.java" > "%SOURCES_LIST%"

echo Compiling GameServer sources...
javac -J-Dfile.encoding=UTF-8 -encoding UTF-8 -g -Xlint:none -cp "%CP%" -d "%OUTPUT_DIR%" @"%SOURCES_LIST%"

if errorlevel 1 (
  echo Build failed.
) else (
  echo Build succeeded.
)

pause



