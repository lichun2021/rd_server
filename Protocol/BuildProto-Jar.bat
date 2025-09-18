@echo proto file build ...
@echo off

set /p isBuildAll=�Ƿ�����proto�ļ�����java��(y(Y)��/n(N)��,ָ�������ļ�)


if "%isBuildAll%" == "y" goto buildAll
if "%isBuildAll%" == "Y" goto buildAll
if "%isBuildAll%" == "n" goto buildSigle
if "%isBuildAll%" == "N" goto buildSigle

goto buildSigle
:buildAll
for /r %%i in (*.proto) do (
	@echo %%~ni.proto
	protoc.exe --java_out=./Protobuf/Java/src/main/java %%~ni.proto
	if errorlevel 1 goto return
)
goto next

:buildSigle
set /p filename=������proto�ļ���(��������׺):
if not exist %filename%.proto (
	echo �ļ� %filename%.proto �����ڣ�
	goto buildSigle
)
@echo build %filename%.proto
protoc.exe --java_out=./Protobuf/Java/src/main/java %filename%.proto

set /p continueBuild=�Ƿ�������룿(y(Y)��/n(N)��)
if "%continueBuild%" == "y" goto buildSigle
if "%continueBuild%" == "Y" goto buildSigle
:next

@echo proto build done!

@echo gradle build gameprotocol.jar ...

call gradle -b Protobuf\Java\build.gradle clean
call gradle -b Protobuf\Java\build.gradle jar

@echo gameprotocol.jar build done!

@echo publish gameprotocol.jar ...

xcopy /Y Protobuf\Java\build\libs\gameprotocol.jar ..\GameServer\lib

@echo build success!

:return
pause