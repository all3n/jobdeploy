TARGET=target
BIN=bin
rm -rf $TARGET
mkdir -p $TARGET
mkdir -p $BIN
docker run -u $(id -u):$(id -g) --rm -v "$PWD":/usr/src/myapp -w /usr/src/myapp gcc:4.9 make
tar -czf $TARGET/pkg.tgz --exclude target --exclude deploy --exclude deploy.json .
