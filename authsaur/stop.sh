#!/bin/sh
SERVICE_NAME=cas.war
tpid=`ps -ef | grep -w "$SERVICE_NAME" |grep -v grep | grep -v kill|awk '{print $2}'`
if [ "${tpid}" ]; then
        echo "-------$SERVICE_NAME 进程Pid是 : $tpid"
    	echo "-------开始停止 $SERVICE_NAME 进程, Pid是 : $tpid"
    	kill -9 $tpid
else
        echo "-------$SERVICE_NAME 没有运行或已停止成功"
fi