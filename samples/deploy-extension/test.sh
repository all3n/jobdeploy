export DEPLOY_PLUGIN=$PWD/build
deploy -y test -D test_var=123
echo "exit code:$?"
