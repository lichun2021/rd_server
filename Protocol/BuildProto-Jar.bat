@echo proto file build ...
@echo off

set /p isBuildAll=是否将所有proto文件生成java？(y(Y)是/n(N)否,指定单个文件)


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
set /p filename=请输入proto文件名(不包含后缀):
if not exist %filename%.proto (
	echo 文件 %filename%.proto 不存在！
	goto buildSigle
)
@echo build %filename%.proto
protoc.exe --java_out=./Protobuf/Java/src/main/java %filename%.proto

set /p continueBuild=是否继续输入？(y(Y)是/n(N)否)
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