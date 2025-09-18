#!/bin/sh
function success
{
	MSG=$*
	echo "`eval $NOW` Success: $MSG"
	exit 0
}

function fail
{
	MSG=$*
	echo "`eval $NOW` Error: $MSG"
	exit 1
}

cfgSh=./cfglog.sh
if [[ -f "$cfgSh" ]]; then
    sh $cfgSh
fi

mem_num=`free -g|grep 'Mem'|awk '{print $2}'`
if [[ "$mem_num" -ge "60" && "$mem_num" -lt "65" ]];then
	nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms44g -Xmx44g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
elif [[ "$mem_num" -gt "25" ]];then
	nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms22g -Xmx22g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
elif [[ "$mem_num" -eq "25" ]];then
	nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms19g -Xmx19g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
elif [[ "$mem_num" -ge "20" && "$mem_num" -lt "25" ]];then
	nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms13g -Xmx13g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
elif [[ "$mem_num" -ge "15" && "$mem_num" -lt "20" ]];then
	nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms11g -Xmx11g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
elif [[ "$mem_num" -ge "8" && "$mem_num" -lt "15" ]];then
	nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms6g -Xmx6g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
elif [[ "$mem_num" -ge "4" && "$mem_num" -lt "8" ]];then
	nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms2g -Xmx2g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
elif [[ "$mem_num" -gt "0" && "$mem_num" -lt "4" ]];then
	nohup java -server -javaagent:lib/hotfixagent.jar -Dfile.encoding=UTF-8 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:-OmitStackTraceInFastThrow -Xss512k -Xms1g -Xmx1g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=30 -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log -XX:+HeapDumpOnOutOfMemoryError -jar gameserver.jar > /dev/null 2>&1 &
else
	fail "start Faild!"
fi

sleep 10

if [ $? -ne 0 ]
then
	fail "start Faild!"
else
	success "start Success!"
fi

