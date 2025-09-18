@echo protocol file generator for lua-protobuf...
@echo off
set fileDir=%~dp0

set TempDir="%TEMP%\HJProjectX"
if exist %TempDir% del /f /q /s "%TempDir%\*.lua"
if not exist %TempDir% mkdir %TempDir%

for /r %%i in (*.proto) do (
	@echo generate lua_pb for:%%~ni.proto
	protoc.exe --lua_out="%TempDir%" --plugin=protoc-gen-lua=".\plugin\protoc-gen-lua-lpb.bat" %%~ni.proto
)
@echo done protoc-gen-lua-lpb!
if not exist "%fileDir%Protobuf\Lua" mkdir "%fileDir%Protobuf\Lua"
move /y "%TempDir%\*.lua" "%fileDir%Protobuf\Lua\"
@echo done all!
@REM pause