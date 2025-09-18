#!/bin/sh
pid=${1}
logpath=logs/Profiler.log

echo "jstat profiler" >> $logpath
echo "time" >> %logpath%
date "+%G-%m-%d %H:%M:%S" >> $logpath
echo "jstat-start" >> %logpath%
jstat -gc $pid >> $logpath
echo "jstat-end" >> %logpath%
echo " " >> $logpath

echo "jmap profiler" >> $logpath
echo "time" >> %logpath%
date "+%G-%m-%d %H:%M:%S" >> $logpath
echo "jmap-start" >> %logpath%
jmap -histo:live $pid | head -100 >> $logpath
echo "jmap-end" >> %logpath%
echo " " >> $logpath