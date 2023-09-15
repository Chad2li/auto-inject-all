#!/bin/bash

# param 1:
## install: 安装到本地 maven 仓储
## deploy: 发布到 sonatype

exec=install
if [ -n "$1" ]; then
	exec=$1
fi

mvn clean -Dmaven.test.skip=true $exec
