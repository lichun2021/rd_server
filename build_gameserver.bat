@echo off
setlocal

REM 只编译 GameServer 子项目
call gradle :GameServer:clean
if errorlevel 1 goto error

call gradle :GameServer:jar
if errorlevel 1 goto error

echo =====================================
echo ===== GameServer build success ======
echo =====================================

goto end

:error
echo =====================================
echo ===== GameServer build failed  ======
echo =====================================

:end
pause
