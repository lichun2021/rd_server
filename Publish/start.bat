chcp 65001
title %~dp0
java -server -javaagent:lib/hotfixagent.jar -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6789 -Dfile.encoding=UTF-8 -jar gameserver.jar
pause