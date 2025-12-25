@echo off
REM Start GameRobot from robot-start
cd /d "%~dp0\robot-start"

REM Prefer JAVA_HOME if set, otherwise use java in PATH
if defined JAVA_HOME (
  set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_BIN=java"
)

REM JVM options: adjust heap if需要
set "JAVA_OPTS=-Xms512m -Xmx2048m -Dfile.encoding=UTF-8"

%JAVA_BIN% %JAVA_OPTS% -cp "gamerobot.jar;lib/*" com.hawk.robot.GameRobotMain

pause

