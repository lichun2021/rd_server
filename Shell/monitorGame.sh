#!/bin/bash

# 实时在线人数推送服务进程的pid
ONLINE_SEND_PID=$1

while [ true ]
do

GAME_PID=$(cat /data/game/pid.log)

count=$(ps -ef | grep java | grep gameserver | grep ${GAME_PID} | wc -l)
if [ "${count}" -eq "0" ]; then 
   curl --data-urlencode 'msg=通知:game服务器进程异常&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"  
else
   port=$(netstat -ntlp|grep java|grep 9595 | wc -l)
   if [ "${port}" -eq "0" ]; then
       curl --data-urlencode 'msg=通知:9595端口未开启&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
   fi

   port=$(netstat -ntlp|grep java|grep 8080 | wc -l)
   if [ "${port}" -eq "0" ]; then
       curl --data-urlencode 'msg=通知:8080端口未开启&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
   fi
fi 

count=$(ps -ef | grep online-send | grep ${ONLINE_SEND_PID} | wc -l)
if [ "${count}" -eq "0" ]; then
   curl --data-urlencode 'msg=通知:未开启实时在线数据上报&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"  
fi

DISK_USED=$(echo $(df -h) | awk '{print $12}' | sed 's/%//g')
if [ "${DISK_USED}" -gt "85" ]; then
   curl --data-urlencode 'msg=通知:game服务器磁盘vda1使用率达到"'${DISK_USED}'%"小心爆满&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
fi

DISK_USED=$(echo $(df -h) | awk '{print $18}' | sed 's/%//g')
if [ "${DISK_USED}" -gt "85" ]; then
   curl --data-urlencode 'msg=通知:game服务器磁盘vdb1使用率达到"'${DISK_USED}'%"小心爆满&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
fi

sleep 60

done
