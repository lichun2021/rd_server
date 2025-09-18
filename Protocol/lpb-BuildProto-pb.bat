@echo protocol pb files generator...
@echo off
set fileDir=%~dp0

if exist "%TEMP%\HJProjectX" del /f /q /s "%TEMP%\HJProjectX\*.pb"
if not exist "%TEMP%\HJProjectX" mkdir "%TEMP%\HJProjectX"

set pbFilesCount=0
@echo "--------------------begin general pb file-----------------------"
for /r %%i in (*.proto) do (
	set /a pbFilesCount+=1
	@echo "general pb file for:%%~ni.proto"
	protoc.exe -o "%TEMP%\HJProjectX\%%~ni.pb" %%~ni.proto
)
@echo "general all pb files done! pb files count:%pbFilesCount%"
@echo "--------------------end general pb file-----------------------

set pbFilesPath=%fileDir%Protobuf\pb\

@echo "--------------------begin move pb file to:%pbFilesPath%-----------------------"
if not exist "%fileDir%Protobuf\pb" mkdir "%fileDir%Protobuf\pb"
move /y "%TEMP%\HJProjectX\*.pb" %pbFilesPath%
@echo "move TEMP to local pb folder done!"
@echo "--------------------end move pb file-----------------------

@echo "--------------------begin general pb_all_names.lua for all pb files-----------------------"
setlocal enabledelayedexpansion
set pb_all_names_lua_file=%pbFilesPath%pb_all_names.lua
echo --generator by lpb-BuildProto-pb.bat! Do not Edit!
echo local pb_all_names = {>%pb_all_names_lua_file%
for /r %pbFilesPath% %%i in (*.pb) do (
	@REM @echo "pb file name :%%~ni.pb"
	set content=    ^"%%~ni.pb^",
	echo !content!>>%pb_all_names_lua_file%
)
echo }>>%pb_all_names_lua_file%
echo return pb_all_names>>%pb_all_names_lua_file%
@echo "--------------------end general pb_all_names.lua for all pb files-----------------------
@REM pause
 