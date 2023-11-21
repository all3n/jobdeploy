export DEPLOY_PLUGIN=$PWD/build
deploy -y test -D var_name="this is var_name"
echo "exit code:$?"
