redisHost=
redisPort=
redisAuth=
serverId=
content=$(cat $1)

if [[ "$#" = "" ]]
    then
    echo -e "---------------------------\n"
    echo -e "请传入公告文件名字。。。\n"
    echo -e "---------------------------\n"
    exit
fi


if [[ "$redisHost" = "" ]]
    then
    echo -e "---------------------------\n"
    echo -e "请配置redis ip。。。\n"
    echo -e "---------------------------\n"
    exit
fi

if [[ "$redisPort" = "" ]]
    then
    echo -e "---------------------------\n"
    echo -e "请配置redis port。。。\n"
    echo -e "---------------------------\n"
    exit
fi

while read line
do
    if [[ $line =~ "\"id\":" ]]
    
        then
        key=$line
        key=${key%\"*}
        key=${key##*\"}
    fi

    if [[ $line =~ "scene" ]]
        then
        scene=$line
        scene=${scene%\,*}
        scene=${scene##*:}
        scene=${scene// /}
    fi
done<$1

if [[ $scene == "2" ]]
	then
	if [[ $serverId == "" ]]
	then
	echo -e "---------------------------\n"
    echo -e "请配置serverId。。。\n"
    echo -e "---------------------------\n"
    exit
	fi
fi

if [[ $scene == "1" ]]; then
    hKey="billboard:$scene"
else
    hKey="billboard:$scene:$serverId"
fi

echo -----------------------------------
echo $redisHost
echo $redisPort
echo $hKey
echo $key
echo "content: $content"
echo -----------------------------------


if [[ "$redisAuth" = "" ]]
    then
    redis-cli -h $redisHost -p $redisPort hset $hKey $key "$content"
else
    redis-cli -h $redisHost -p $redisPort  -a $redisAuth hset "$hKey" "$key" "$content"
fi
