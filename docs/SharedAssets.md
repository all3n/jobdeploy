# SharedAssets

主要用于不同deploy release之间需要共享的目录

比如data,logs

shared_assets

| key   | name             | description                                                  |
| ----- | ---------------- | ------------------------------------------------------------ |
| from  | 链接名称         | 创建在deploy目录，指向to路径                                 |
| to    | 目标路径         | 实际路径，/前缀为绝对路径，否则则为相对/home/{user}/的相对路径路径 |
| chmod | 目录权限（可选） |                                                              |

示例如下

```
{
  "deployto": "/data/opt/deploy-${user}/${name}",
  "current_link": "/data/opt/${name}",
  "user": "deploy",
  "servers": [
    {
      "server": "xxxx.xxxx.net"
    }
  ],
  "deploy_mode": "local",
  "upload": [
  ],
  "shared_assets": [
    {
      "from": "logs",
      "to": "/data/opt/app-data/${user}/${name}/logs",
      "chmod": "755"
    },
    {
      "from": "data",
      "to": "/data/opt/app-data/${user}/${name}/data",
      "chmod": "755"
    }
  ]
}

```

