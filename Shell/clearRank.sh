
serverId=`grep 'serverId' 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|sed s/[[:space:]]//g`
serverId=${serverId##*=}

redisHost=`cat -A 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|grep ':6379' |sed 's/:6379//g'|sed s/[[:space:]]//g`
redisHost=${redisHost#*=}
redisPassWord=`grep 'globalRedisAuth' 'cfg/svr.cfg'|tr -d '^M'|tr -d '$'|sed s/[[:space:]]//g`
redisPassWord=${redisPassWord#*=}

echo "$serverId"
redis-cli -h $redisHost -a $redisPassWord ZREMRANGEBYLEX $serverId":player_fight_rank" - +
redis-cli -h $redisHost -a $redisPassWord ZREMRANGEBYLEX $serverId":player_kill_rank" - +
redis-cli -h $redisHost -a $redisPassWord ZREMRANGEBYLEX $serverId":player_castle_rank" - +
redis-cli -h $redisHost -a $redisPassWord ZREMRANGEBYLEX $serverId":player_grade_rank" - +
redis-cli -h $redisHost -a $redisPassWord ZREMRANGEBYLEX $serverId":player_fight_rank" - +
redis-cli -h $redisHost -a $redisPassWord ZREMRANGEBYLEX $serverId":player_kill_rank" - +
