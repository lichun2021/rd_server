#!/bin/sh
debugXml=./log4j/log4j2-debug.xml
xml=./log4j/log4j2.xml

files=(activity xml serverlist cfg version)

function read_dir()
{
	for file in `ls $1`       #注意此处这是两个反引号，表示运行系统命令
	do
		if [ -d $1"/"$file ]  #注意此处之间一定要加上空格，否则会报错
		then
			read_dir $1"/"$file
		elif [[ ! "$1/$file" =~ ".log" ]]; then
			mv $1/$file  $1/$file.log
		fi
	done
}   

#$1 为需要拷贝的目录 $2为目录的目录
copyFile()
{
	cp -rf $1 $2/cfglog
	read_dir "$2/cfglog"
}

main()
{
  log4jXml="";
  if [ ! -d "$debugXml" ]; then
	log4jXml=$xml
  else
	log4jXml=$debugXml
  fi

  logsPath=$(cat $log4jXml |grep 'name=\"LOG_HOME' | awk -F'[<>=]' '{print $4}')

  mkdir $logsPath/cfglog  

  for sourceFile in ${files[@]}		
  do
   	if [ -d "$sourceFile" ];
		then
			copyFile $sourceFile $logsPath
	fi
  done
}

main
