@echo Proto file generator...
@echo off
set cygwin=false
set /p filename=input the filename(without suffix):
@echo general %filename%.proto
if "%filename%" =="HP" ( 
	goto tryCheck 
) else ( 
	goto normal 
)
:tryCheck
checkPb HP.proto result
set /p firstRow=<"result"
if "%firstRow%"=="success" (
	set void=noting
) else (
	echo "ERROR================%firstRow%"
	goto endHandle 
)
:normal
protoc.exe --java_out=./Protobuf/Java/src/main/java %filename%.proto
:endHandle
if exist result (del result)
@echo done!
@pause