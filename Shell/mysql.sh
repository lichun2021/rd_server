#数据库连接脚本

dbInfo=`cat -A 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|grep 'mysql' |sed 's/\//:/g'`
dbArrInfo=(${dbInfo//:/ })

ip=${dbArrInfo[4]}
port=${dbArrInfo[5]}
database=${dbArrInfo[6]}

userName=`grep 'dbUserName' 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|sed s/[[:space:]]//g`
userName=${userName#*=}

passWord=`grep 'dbPassWord' 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|sed s/[[:space:]]//g`
passWord=${passWord#*=}

echo ------------------------------------------
echo ip       = $ip
echo port     = $port
echo database = $database
echo userName = $userName
echo passWord = $passWord
echo ------------------------------------------

mysql -h$ip -P$port -u$userName -p$passWord $database
