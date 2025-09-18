@echo off

set fdir=%~dp0

echo %fdir%

call BuildProto-lua.bat

pushd %fdir%

copy /y Protobuf\lua\BattleCfg_pb.lua ..\Resource_Client\Res\lua\protobuf\
copy /y Protobuf\lua\BattleField_pb.lua ..\Resource_Client\Res\lua\protobuf\

call BuildProto-client.bat

pushd %fdir%

copy /y "Protobuf\C++\BattleCfg.pb.*" ..\Code_Client\Code_Battle\BattleField\
copy /y "Protobuf\C++\BattleField.pb.*" ..\Code_Client\Code_Battle\BattleField\

goto :eof
