#!/bin/sh
svrCfg=/tmp/svr.cfg
tomcatCfg=/tmp/tomcat.cfg
controlCfg=/tmp/control.cfg
gameConst=/tmp/gameConst.cfg
versionPublish=./versionPublish
appCfg=/tmp/app.cfg

set -e
function main () 
{
    
	curTime=$(date '+%s%3N');
	svrCfg="/tmp/$curTime-svr.cfg"
	tomcatCfg="/tmp/$curTime-tomcat.cfg"
	controlCfg="/tmp/$curTime-control.cfg"
	gameConst="/tmp/$curTime-gameConst.cfg"
	appCfg="/tmp/$curTime-app.cfg"	
	if [ $1 -eq 1 ]; then
		if [ $# != 3 ]; then
			echo "you need appoint type,protocolSvn,runPath";
			exit1
		fi
	elif [ $1 -eq 2 ]; then
		if [ $# != 3 ]; then
			echo "you need appoint type,protocolPath,commitSvnPath";
		fi
	fi
		
	sh updateProtocol.sh $2
	#go to engine path build engine must

	#go back gameserver path

    #如果是打包需要checkout目录
    if [ $1 -eq 2 ]; then
        tryCheckOutToPublish $3
    fi

	sh build.sh

	echo $*

	# 1是普通打包 打包完了重启服务, 2发布到版本服务器
	if [ $1 -eq 1 ]; then
		copyToPath $3
	elif [ $1 -eq 2 ]; then
		rm -rf ./Publish/xml
		rm -rf ./Publish/activity
        cp -rf ./Publish/* ./versionPublish
        cd ./versionPublish
        svn add * --force
        svn commit -m "R57852636 auto package commit"
	fi
}

function tryCheckOutToPublish()
{	
	if [ -d $versionPublish ]; then
		rm -rf $versionPublish
	fi
	
	svn checkout -q $1 $versionPublish
}

function commitToSvn() 
{
	publish=./Publish
	cd $publish
	svn add * --force
	svn commit -m "auto package commit"
}

function stopAndStart() 
{	
	#进入目标目录
	cd $1
	sh stop.sh
	sh start.sh
}

#编译完拷贝到目录
function copyToPath() 
{
	targetPath=$1
	if [ ! -n "$targetPath"  ]; then
        echo "target path is empty"
        exit 1
	elif [ ! -d "$targetPath" ]; then
        echo "is not a path";
        exit 1
	else
		if [ -f "$targetPath/cfg/svr.cfg" ];then
			cp -rf $targetPath/cfg/svr.cfg  $svrCfg
            cp -rf $targetPath/tomcat/tomcat.cfg $tomcatCfg
			cp -rf $targetPath/cfg/control.cfg $controlCfg
			cp -rf $targetPath/cfg/app.cfg $appCfg
			cp -rf $targetPath/cfg/gameConst.cfg $gameConst
			
			cp -rf ./Publish/.  $targetPath/
			cp -rf $svrCfg $targetPath/cfg/svr.cfg
            cp -rf $tomcatCfg $targetPath/tomcat/tomcat.cfg
			cp -rf $controlCfg $targetPath/cfg/control.cfg
			cp -rf $appCfg $targetPath/cfg/app.cfg
			cp -rf $gameConst $targetPath/cfg/gameConst.cfg
		else 
			cp -rf ./Publish/. $targetPath/
		fi
	fi
}

main $*
