#!/bin/bash

#项目名称
APP_NAME='arthas-tunnel-web'
#项目启动端口号
SERVER_PORT='9999'
#Tunnel Server的启动端口
TUNNEL_SERVER_PORT='7777'
#项目启动需要附加的jvm参数
JVM_OPTS='-Xmx256m -Xms256m'
#项目打包上传的docker仓库地址
REGISTRY_HOST=''
#项目打包的镜分组
REGISTRY_GROUP='wf2311'
#项目打包的镜像名称
REGISTRY_IMAGE='arthas-tunnel-web:latest'


echo "stage mvn package"
cd ../
mvn -DskipTests package

echo "build and push docker image"

docker build --build-arg APP_NAME=$APP_NAME -t $REGISTRY_GROUP/$REGISTRY_IMAGE .
docker push $REGISTRY_GROUP/$REGISTRY_IMAGE
