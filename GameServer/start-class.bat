@echo off
setlocal
chcp 65001 >nul
title %~dp0
cd /d %~dp0

rem 可选：先检查classes是否存在
if not exist classes\java\main (
  echo classes\java\main 不存在，请先编译。
  pause
  exit /b 1
)

set JAVA_OPTS=-server -javaagent:lib\hotfixagent.jar -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6789 -Dfile.encoding=UTF-8

rem 按你的目录结构：classes\java\main + lib 根下所有jar + 分层aspectj
set CP=classes\java\main;lib\*;lib\org.aspectj\aspectjrt\1.8.9\aspectjrt-1.8.9.jar;lib\org.aspectj\aspectjweaver\1.8.9\aspectjweaver-1.8.9.jar

echo Starting (classes-first) ...
java %JAVA_OPTS% -cp "%CP%" com.hawk.game.GsMain
pause