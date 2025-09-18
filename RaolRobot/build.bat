@echo off

rm -rf %PROJECT_HOME%\bin
rm -rf %PROJECT_HOME%\build

call gradle -b %PROJECT_HOME%\build.gradle build

pause