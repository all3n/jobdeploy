export DEPLOY_PLUGIN=$PWD/build
# export TEST_NAME=test_name
deploy -y test -D var_name="this is var_name" $@
echo "exit code:$?"
