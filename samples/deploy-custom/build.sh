TARGET=target
BIN=bin
rm -rf $TARGET
mkdir -p $TARGET
mkdir -p $BIN
g++ -o bin/test_deploy src/main.cpp
tar -czf $TARGET/pkg.tgz --exclude target --exclude deploy --exclude deploy.json .
