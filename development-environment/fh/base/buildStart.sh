#!/bin/sh
set echo on

sudo docker stop $(sudo docker ps -aq)
docker rmi $(docker images -f "dangling=true" -q) --force

export SLR_BUILD_HOME=`pwd`
export WORKSPACE=/home/fredy/ws/loa2

. ../buildLoaApp.sh

docker-compose pull mailhog.mailhog
docker-compose pull keycloak
docker-compose pull loa-admin-server
docker-compose up 
