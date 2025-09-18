@echo protocol file generator...
@echo off

set fileDir=%~dp0
set /p filename=input the filename(without suffix):

call BuildProto-lua-signal_with_input.bat %filename%
pause