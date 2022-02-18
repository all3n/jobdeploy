#!/bin/bash
bin=`dirname "$0"`
export APPDIR=`cd "$bin/../"; pwd`

pushd $APPDIR
DATETIME=$(date +%Y%m%d_%H%M%S)
VERID=$(git rev-parse --short HEAD)
TAG_NAME="${DATETIME}_${VERID}"
echo $TAG_NAME
VERSION_FILE=core/src/main/java/com/devhc/jobdeploy/config/Constants.java

sed -i.bak 's/^[[:space:]]*public[[:space:]]\{1,\}static[[:space:]]\{1,\}final[[:space:]]\{1,\}String[[:space:]]\{1,\}DEPLOY_VERSION[[:space:]]\{1,\}=.*$/  public static final String DEPLOY_VERSION = "'$TAG_NAME'";/' $VERSION_FILE
mvn clean package -DskipTests -Paliyun
mv $VERSION_FILE.bak $VERSION_FILE
popd
