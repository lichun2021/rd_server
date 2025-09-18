@echo off

set PROJECT_HOME="."

xcopy /Y ..\HawkEngine\lib\hawk.jar %PROJECT_HOME%\lib\
xcopy /Y ..\GameActivity\build\libs\gameactivity.jar %PROJECT_HOME%\lib\
xcopy /Y ..\Protocol\Protobuf\Java\build\libs\gameprotocol.jar %PROJECT_HOME%\lib\

xcopy /Y ..\GameServer\xml\battle_soldier.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\build.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\build_limit.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\dailyActiveReward.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\gacha.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\gift.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\guild_science_level.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\item.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\player_talent.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\player_level.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\shop.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\talent.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\talent_level.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\tech_level.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\world_resource.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\const.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\tmx_block.dat %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\build_area.xml %PROJECT_HOME%\xml\
xcopy /Y ..\GameServer\xml\equipment.xml %PROJECT_HOME%\xml\

rm -rf %PROJECT_HOME%\bin
rm -rf %PROJECT_HOME%\build

call gradle -b %PROJECT_HOME%\build.gradle build

set TARGET_HOME="..\QAServer\trunk"
xcopy /Y ..\HawkEngine\lib\*.jar %TARGET_HOME%\robot-start\lib\
xcopy /Y %PROJECT_HOME%\build\libs\gamerobot.jar %TARGET_HOME%\robot-start\
xcopy /Y %PROJECT_HOME%\cfg\* %TARGET_HOME%\robot-start\cfg\
xcopy /Y %PROJECT_HOME%\lib\* %TARGET_HOME%\robot-start\lib\
xcopy /Y %PROJECT_HOME%\xml\* %TARGET_HOME%\robot-start\xml\

pause