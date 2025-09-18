#!/bin/sh
PID=`cat pid.log`

kill $PID

RESULT=`ps -elf |  awk '{print $4}' | grep  $PID | wc -l`

while [ $RESULT -gt 0 ]
do
    echo "waiting stop ..."
    sleep 1
	
	RESULT=`ps -elf |  awk '{print $4}' | grep  $PID | wc -l`
done  

echo "server stop success"
