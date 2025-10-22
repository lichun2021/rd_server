@echo off
setlocal
cd /d %~dp0

rem 仅编译 GameServer 的 classes（不跑测试，不打包）
call gradle :GameServer:classes -x test
rem 如需更快可跳过 AOP 织入：
rem call gradle :GameServer:classes -x test -x :GameServer:compileAspect

if %ERRORLEVEL% NEQ 0 (
  echo Build classes failed.
  exit /b %ERRORLEVEL%
)

echo Classes compiled to GameServer\build\classes\java\main
exit /b 0