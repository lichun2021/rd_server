@echo protocol file generator...
@echo off

if "%1" == "" (
	echo no target proto file specified!
	echo usage: BuildProto-lua-signal_with_input Cdk    // not Cdk.proto
	pause
	exit
)
set fileDir=%~dp0
set filename=%1

if not exist "%TEMP%\HJProjectX" mkdir "%TEMP%\HJProjectX"
if exist "%TEMP%\HJProjectX\*.lua" del /f /q /s "%TEMP%\HJProjectX\*.lua"

@echo general %filename%.proto
protoc.exe --lua_out="%TEMP%\HJProjectX" --plugin=protoc-gen-lua=".\plugin\protoc-gen-lua.bat" %filename%.proto

@echo handle lua pb file for lua limit of 200 local variable 
call .\Plugin\plugin_handle_lua_single_file.bat %filename%

if not exist "%fileDir%Protobuf\Lua" mkdir "%fileDir%Protobuf\Lua"
move /y "%TEMP%\HJProjectX\new_lua_pb\%filename%_pb.lua" "%fileDir%Protobuf\Lua\"

@echo done!