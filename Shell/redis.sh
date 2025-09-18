#redis连接脚本

redisHost=`cat -A 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|grep ':6379' |sed 's/:6379//g'|sed s/[[:space:]]//g`
redisHost=${redisHost#*=}
redisPassWord=`grep 'globalRedisAuth' 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|sed s/[[:space:]]//g`
redisPassWord=${redisPassWord#*=}
redis-cli -h $redisHost -a $redisPassWord
