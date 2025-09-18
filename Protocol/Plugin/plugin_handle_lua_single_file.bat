cd /d %~dp0
if "%1" == "" (goto ERROR) else (set file_name=%1)
python plugin_handle_lua_files.py -f "%TEMP%\HJProjectX" -t %file_name%
goto HANDLE_END

:ERROR
echo plugin_handle_lua_single_file.bat has no file target

:HANDLE_END
echo plugin_handle_lua_single_file done!