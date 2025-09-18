#!/bin/bash

function main ()
{
    port=`cat -A 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|grep 'tcpPort' |awk -F'=' '{print $2}'`
    pid=`netstat -apn|grep $port|grep 'LISTEN'|awk -F' ' '{print $7}'|awk -F'/' '{print $1}'`
    if [[ $pid == "" ]]; then
        nohup java  -XX:AutoBoxCacheMax=10000 -XX:-OmitStackTraceInFastThrow -Xss512k -Xms4g -Xmx4g -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:MaxGCPauseMillis=300 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:GC.log   -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6789 -Dfile.encoding=UTF-8 -jar gameserver.jar  serverId=10000 > console.log &
        echo server start success
        exit 0
    else 
        echo server is aleardy start
        exit 1
     fi
}

main
