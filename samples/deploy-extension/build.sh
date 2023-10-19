# this script just for test not use for production
CUR_DIR=$(cd $(dirname $0); pwd)
BUILD_DIR=$CUR_DIR/build
mkdir -p $BUILD_DIR
export CLASSPATH="$BUILD_DIR:$BUILD_DIR/*"
down_jar_if_needed() {
  JAR_PATH=$1
  LOCAL_JAR=$HOME/.m2/repository/$JAR_PATH
  REPO_JAR=https://repo1.maven.org/maven2/$JAR_PATH
  if [[ ! -f $LOCAL_JAR ]]; then
    LOCAL_JAR=$BUILD_DIR/$(basename $REPO_JAR)
    if [[ ! -f $LOCAL_JAR ]];then
      wget -O $LOCAL_JAR $REPO_JAR
    fi
  else
    export CLASSPATH="$LOCAL_JAR:$CLASSPATH"
  fi
}

compile_java(){
    javac -d $BUILD_DIR -cp "$CLASSPATH" $@
}

down_jar_if_needed  org/json/json/20230227/json-20230227.jar
compile_java src/BasePlugin.java
compile_java src/XPlugin.java

