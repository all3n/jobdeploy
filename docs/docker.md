# Docker Support

[DockerHub](https://hub.docker.com/r/wanghch8398/jobdeploy/tags)

**aliyun image mirror**
`registry.cn-hangzhou.aliyuncs.com/all3n/jobdeploy`

## deploy with docker

```
docker run --rm -it -v$PWD:/app -v$HOME/.jobdeploy:/home/deploy/.jobdeploy wanghch8398/jobdeploy deploy test
```








