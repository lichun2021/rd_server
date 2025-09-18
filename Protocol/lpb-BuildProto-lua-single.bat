@echo protocol file generator for lua-protobuf...
@echo off
set fileDir=%~dp0
set fileDir=%~dp0
set filename=%1

set outPutPath="%TEMP%\HJProjectX"
if exist %outPutPath% del /f /q /s "%outPutPath%\*.lua"
if not exist %outPutPath% mkdir %outPutPath%

@echo generate lua_pb for:%filename%.proto
protoc.exe --lua_out="%outPutPath%" --plugin=protoc-gen-lua=".\plugin\protoc-gen-lua-lpb.bat" %filename%.proto

@echo done!
pause
