#!/bin/bash
# NOTES:
# this docker build script is not used
# migrate to GITHUB ACTIONS to build and push images
bin=`dirname "$0"`
export APPDIR=`cd "$bin/../"; pwd`

ref_name(){
    git describe --exact-match 2>/dev/null || \
    git rev-parse --abbrev-ref HEAD | grep -v HEAD || \
    git rev-parse --short HEAD
}

pushd $APPDIR
REF_NAME=$(ref_name)
if [[ "$REF_NAME" == "master" ]];then
  REF_NAME="latest"
fi

NAME=jobdeploy
docker_build(){
  docker build -t $NAME:$REF_NAME .
}

docker_push(){
  local REPO_BASE=$1
  docker tag $NAME:$REF_NAME $REPO_BASE:$REF_NAME
  docker push $REPO_BASE:$REF_NAME
  if [[ "$REF_NAME" != "latest" ]];then
    docker push $REPO_BASE:latest
  fi
}


docker_build
docker_push jobdeploy/jobdeploy
popd

