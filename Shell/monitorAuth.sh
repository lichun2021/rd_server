#!/bin/bash

# 固定的消息头: "通知:"
head=$1

while [ true ]
do

# authserver monitor
AUTH_PID=$(cat /data/raol/auth/pid.log)
count=$(ps -ef | grep java | grep authserver | grep ${AUTH_PID} | wc -l)

if [ "${count}" -eq "0" ]; then 
   curl --data-urlencode 'msg='${head}' authserver processor unnormal&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send" 
else
   port=$(netstat -ntlp|grep java|grep 80| wc -l)
   if [ "${port}" -eq "0" ]; then
       curl --data-urlencode 'msg='${head}' port 80 not opened&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
   fi
fi

# gmserver monitor
GM_PID=$(cat /data/raol/gm/pid.log)
count=$(ps -ef | grep java | grep gmserver | grep ${GM_PID} | wc -l)

if [ "${count}" -eq "0" ]; then 
   curl --data-urlencode 'msg='${head}' gmserver processor unnormal&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
else
   port=$(netstat -ntlp|grep java|grep 8081| wc -l)
   if [ "${port}" -eq "0" ]; then
       curl --data-urlencode 'msg='${head}' port 8081 not opened&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
   fi
fi

# cdkserver monitor
CDK_PID=$(cat /data/raol/cdk/pid.log)
count=$(ps -ef | grep java | grep cdkserver | grep ${CDK_PID} | wc -l)

if [ "${count}" -eq "0" ]; then 
   curl --data-urlencode 'msg='${head}' cdkserver processor unnormal&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"  
else 
   port=$(netstat -ntlp|grep java|grep 8082| wc -l)
   if [ "${port}" -eq "0" ]; then
       curl --data-urlencode 'msg='${head}' port 8082 not opened&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
   fi
fi

# disk monitor
DISK_USED=$(echo $(df -h) | awk '{print $12}' | sed 's/%//g')
if [ "${DISK_USED}" -gt "85" ]; then
   curl --data-urlencode 'msg='${head}' authserver disk vda1 usage reach '${DISK_USED}'%&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
fi

DISK_USED=$(echo $(df -h) | awk '{print $18}' | sed 's/%//g')
if [ "${DISK_USED}" -gt "85" ]; then
   curl --data-urlencode 'msg='${head}' authserver disk vdb1 usage reach '${DISK_USED}'%&target=15201137122,13466574073' "http://10.154.128.234:8081/script/sms/send"
fi

sleep 60

done
