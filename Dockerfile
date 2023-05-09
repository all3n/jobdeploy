FROM jobdeploy/jobdeploy-env:maven-3.6.3
ADD target/jobdeploy-*-deploy.tar.gz $JD_HOME
