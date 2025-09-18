@echo protocol file generator...
@echo off
for /r %%i in (*.proto) do (
	@echo general %%~ni.proto
	protoc.exe --cpp_out=./Protobuf/C++/ %%~ni.proto
)
@echo done!