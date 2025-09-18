#!/bin/sh
cfgSh=./cfglog.sh
if [[ -f "$cfgSh" ]]; then
    sh $cfgSh
fi
nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms10g -Xmx10g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
