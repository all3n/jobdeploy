export DEPLOY_PLUGIN=$PWD/build
# export TEST_NAME=test_name
deploy -V -y test -D var_name="this is var_name" $@
echo "exit code:$?"
