#!/bin/sh
protoNum=""
set -e
function str2Array()
{
    echo ${1//$2/ }
}


function clearSpace()
{
    #echo $1 | sed 's/ //g'
	echo $1 | sed '/^[[:space:]]*$/d'
}
function main() 
{
	echo "false" > result
	toFile=UnixHP.proto
	tr -d '\r' < HP.proto > $toFile
	result=""
	declare -A enumId=()
	while read line 
	do
		#echo "line=$line"
		if [[  $line =~ "=" ]] ;
			then
				if [[ $line =~ "java_package" ]] ;
					then
					#do nothing
					true
				else					
					right=${line#*=}
					left=${right%;*}
					finalString=$(clearSpace $left)
					existValue=${enumId[$finalString]}
					#echo "hasSame=$hasSame  finalString=$finalString $result"
					if [[ $existValue == ""  ]];
						then							
							enumId[$finalString]=$finalString
						else 
							echo "has same enumID:$finalString">result
							exit
					fi					
					result=$result:$finalString
				fi
		
		fi
	done < $toFile
	echo "success" > result
	rm -rf UnixHP.proto
}
main
