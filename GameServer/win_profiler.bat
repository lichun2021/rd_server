set pid=%1
set logpath=logs/Profiler.log

echo jstat profiler >> %logpath%
echo time >> %logpath%
echo %date:~0,4%-%date:~5,2%-%date:~8,2% %time:~0,8% >> %logpath%
echo jstat-start >> %logpath%
jstat -gc %pid% >> %logpath%
echo jstat-end >> %logpath%
echo. >> %logpath%

echo jmap profiler >> %logpath%
echo time >> %logpath%
echo %date:~0,4%-%date:~5,2%-%date:~8,2% %time:~0,8% >> %logpath%
echo jmap-start >> %logpath%
jmap -histo:live %pid% | head -23 >> %logpath%
echo jmap-end >> %logpath%
echo. >> %logpath%
