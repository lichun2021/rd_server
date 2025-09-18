@echo protocol file generator for lua-protobuf...
@echo off
cd %fileDir%
call lpb-BuildProto-lua.bat
@echo done lpb-BuildProto-lua.bat!

cd %fileDir%
call lpb-BuildProto-pb.bat
@echo done lpb-BuildProto-pb.bat!
pause
