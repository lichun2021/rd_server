protoc.exe --cpp_out=../Code_Battle/BattleField BattleField.proto
protoc.exe --lua_out=../Resource_Client/Res/lua/protobuf --plugin=protoc-gen-lua=".\Plugin\protoc-gen-lua.bat" BattleField.proto
@pause