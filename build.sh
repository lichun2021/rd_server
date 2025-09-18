#!/bin/sh
#输出svn的版本信息到gameserver
set -e
svn info > GameServer/src/main/resources/svn_info.txt

gradle clean
gradle jar

#下面命令-DversionFolder=windows中 windows 字符串为多版本发布选择参数
#对应本文件目录中Launcher文件夹下的版本文件夹名称，默认文件夹名称为 windows

gradle -DversionFolder=windows publish

echo =====================================
echo ============build success============
echo =====================================