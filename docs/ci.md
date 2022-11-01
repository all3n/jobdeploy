## Run In CI Docker With SSH Agent

1. SET SSH_PRIVATE_KEY with private key contents in CI Setting
   1. gitlab (settings/ci_cd/Variables) (should have master/maintainer permission)
   2. set SSH_PRIVATE_KEY Protected (only work in protected branch/tags) recommand
2. Add scripts before exec deploy commands(if config in server side hooks, this step  will skip)

```
eval $(ssh-agent -s)
echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
mkdir -m 700 -p ~/.ssh
unset SSH_PRIVATE_KEY
```

3. execute deploy  command

## gitlab ci yaml examples

```
deploy:
  image: registry.cn-hangzhou.aliyuncs.com/all3n/jobdeploy:latest
  before_script:
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -m 700 -p ~/.ssh
    - unset SSH_PRIVATE_KEY
  script:
    - deploy -y prod
```



## Config Server Side Runner Hooks GitLab

editor your runner config.toml add  pre_build_script in runner config

[runner config](https://docs.gitlab.com/runner/configuration/advanced-configuration.html#the-runners-section)

```
[[runners]]

...


pre_build_script = "HOOK_BASE=/usr/libexec/gitlab-runner-hooks HOOK_NAME=pre_build_script . -- /usr/libexec/gitlab-runner-hooks/common-hook"
  post_build_script = "HOOK_BASE=/usr/libexec/gitlab-runner-hooks HOOK_NAME=post_build_script . -- /usr/libexec/gitlab-runner-hooks/common-hook"
  
  
[[runners.docker]]
volumes = [
	"/xxx/xx/gitlab-runner-hooks:/usr/libexec/gitlab-runner-hooks:ro"
]
```





common-hook

```
: ${HOOK_BASE:=/usr/libexec/gitlab-runner-hooks}
: ${HOOK_NAME:=pre_build_script}

if [ -d "$HOOK_BASE/$HOOK_NAME.d" ]; then
  for script in "$HOOK_BASE/$HOOK_NAME.d"/*.sh ; do
    [ -r "$script" ] && . -- "$script"
  done
fi
```

add 1-ssh-agent.sh

pre_build_script.d/1-ssh-agent.sh

```
## ref: https://docs.gitlab.com/ee/ci/ssh_keys/

__main() {
  unset -f __main
  local i=

  [ -n "$SSH_PRIVATE_KEY" ] || return 0

  for i in ssh ssh-add ssh-agent; do
    command -v $i >/dev/null || return 0
  done

  eval $(ssh-agent -s)

  ##
  ## Add the SSH key stored in SSH_PRIVATE_KEY variable to the agent store
  ## We're using tr to fix line endings which makes ed25519 keys work
  ## without extra base64 encoding.
  ## https://gitlab.com/gitlab-examples/ssh-private-key/issues/1#note_48526556
  ##
  echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -

  mkdir -m 700 -p ~/.ssh

  unset SSH_PRIVATE_KEY
}

__main
```

