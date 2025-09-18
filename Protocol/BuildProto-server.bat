@echo protocol file generator...
@echo off
checkPb HP.proto result
set /p firstRow=<"result"
if "%firstRow%"=="success" (
	set void=noting
) else (
	echo "ERROR================%firstRow%"
	goto endHandle 
)
if exist result (del result)
:compile
for /r %%i in (*.proto) do (
	@echo general %%~ni.proto
	protoc.exe --java_out=./Protobuf/Java/src/main/java %%~ni.proto
	if errorlevel 1 goto protoFail
)
goto protoSuccess

:protoFail
@echo proto build error
goto fuckPause
:protoSuccess
@echo proto build done!
goto fuckPause
:endHandle
if exist result (del result)
:fuckPause
@pause