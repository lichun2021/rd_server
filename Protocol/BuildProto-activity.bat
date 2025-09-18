::only update Activity_pb and HP_pb£¬and move into the project
@echo protocol file generator...
@echo off
set fileDir=%~dp0

if exist "%TEMP%\HJProjectX" del /f /q /s "%TEMP%\HJProjectX\*.lua"
if not exist "%TEMP%\HJProjectX" mkdir "%TEMP%\HJProjectX"

protoc.exe --lua_out="%TEMP%\HJProjectX" --plugin=protoc-gen-lua=".\plugin\protoc-gen-lua.bat" Activity.proto
protoc.exe --lua_out="%TEMP%\HJProjectX" --plugin=protoc-gen-lua=".\plugin\protoc-gen-lua.bat" HP.proto

@echo done!
REM ~ pause
@echo handle lua pb file for lua limit of 200 local variable 
call ./Plugin/plugin_handle_lua_files.bat
if not exist "%fileDir%Protobuf\Lua" mkdir "%fileDir%Protobuf\Lua"
move /y "%TEMP%\HJProjectX\new_lua_pb\*.lua" "%fileDir%\..\Resource_Client\Res\lua\protobuf"
@echo done!
pause
