#!/bin/sh
set -e

function main()
{   
    if [ ! -n $1 ] ; then
        echo "you must config protocol path"
        exit 1
    fi

    protocolPath="./Protocol"	
    if [ ! -d $protocolPath ]; then
	    svn co  $1 $protocolPath
    else
		
		result=$(svn info $protocolPath |grep $1)		
		if [ -n "$result" ]; then
			svn up $protocolPath
		else
			mv $protocolPath oldProtocol
			svn co  $1 $protocolPath
		fi
	    
    fi       

}

main $1
