#!/bin/bash

function main () {
    port=`cat -A 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|grep 'tcpPort' |awk -F'=' '{print $2}'`
    echo $port
    PID=`netstat -apn|grep $port|grep 'LISTEN'|awk -F' ' '{print $7}'|awk -F'/' '{print $1}'`

    echo $PID
    
    if [[ $PID == "" ]]; then
        echo "server is not start"
    else 
        kill $PID

        RESULT=`ps -elf |  awk '{print $4}' | grep  $PID | wc -l`

        while [ $RESULT -gt 0 ]
            do
            echo "waiting stop ..."
             sleep 1

        	RESULT=`ps -elf |  awk '{print $4}' | grep  $PID | wc -l`
        done  

        echo "server stop success"        
     fi
    
}

main


