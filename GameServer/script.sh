#!/bin/bash

cmd=localhost:8080/script

if [ "$#" -gt "1" ]; then
   cmd=${cmd}/$1?$2
   params=($@)
   for ((i=2; i<${#params[*]}; i++)) do
	param=${params[$i]}
        cmd=${cmd}\&${param}
   done
elif [ "$#" -eq "1" ]; then
   cmd=${cmd}/$1
else
   cmd=${cmd}/xmlreload
fi

echo ${cmd}

curl ${cmd}


