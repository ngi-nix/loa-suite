#!/bin/sh
set echo on
cd $CURRENT_APP_HOME
echo build $CURRENT_APP_NAME
mvn -q clean install -DskipTests
mkdir -p $CURRENT_APP_NAME/target/dependency 
cd $CURRENT_APP_NAME/target/dependency
jar -xf $CURRENT_APP_HOME/$CURRENT_APP_NAME/target/app.jar
cd ../..
#ls -l $CURRENT_APP_HOME/$CURRENT_APP_NAME/target/dependency
docker build -t $CURRENT_APP_NAME:latest .
cd $SLR_BUILD_HOME



