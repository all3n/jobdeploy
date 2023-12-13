# HOW TO DEPLOY

```
mode:
latest:
Pulls the latest code.
The current path only contains the deploy configuration, without the code.
Dynamically pulls and deploys the code.

local:
Does not pull the code. Deploys directly using the code in the directory where the deploy command is executed.
Used for CI/local development environment deployment.
```

## Deploying with SCM Branch or Tag
```
This method uses the "latest" mode, which pulls the code.

deploy prod -t v1.0: Deploys the v1.0 tag version to the prod environment.
deploy prod -b b1.0: Deploys the b1.0 branch version to the prod environment.
deploy prod: Deploys the master version to the prod environment.
deploy prod -r commitid: Deploys the specified commitid to the prod environment (for SVN, it's the version number; for Git, it's the commitid hash string).
```

## Deploying to Specific Machines
* deploy --hosts xx.xxx.xx.com test: Deploys to the specified machine (xx.xxx.xx.com) for the "test" environment.

## Deploying with Custom Parameters
* deploy --hosts xx.xxx.xx.com test -D auth_type=password: Deploys to the specified machine (xx.xxx.xx.com) for the "test" environment with the custom parameter "auth_type=password".

## CI Environment Deployment
```
For CI environments, it is recommended to use the "local" mode since the CI container already has the code downloaded.
Add the -y option to skip the confirmation prompt.

deploy -y stage
```
