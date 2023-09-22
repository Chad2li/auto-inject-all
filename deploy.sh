#!/bin/bash

# param 1:
## install: 安装到本地 maven 仓储
## deploy: 发布到 sonatype，默认

exec=install
if [ -n "$1" ]; then
	exec=$1
fi

# 临时调整jdk路径
JAVA_HOME=/Library/Java/JavaVirtualMachines/openlogic-openjdk-8.jdk/Contents/Home/
mvn --version
mvn clean -DskipTests -e $exec
