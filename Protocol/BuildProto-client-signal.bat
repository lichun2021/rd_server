@echo protocol file generator...
@echo off
set /p filename=input the filename(without suffix):
@echo general %filename%.proto
protoc.exe --cpp_out=./Protobuf/C++/ %filename%.proto
@pause
@echo done!