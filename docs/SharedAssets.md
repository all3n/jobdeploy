# SharedAssets
Directory shared during each deployment

For example: data, logs

shared_assets

| key   | name             | description                                                  |
| ----- | ---------------- | ------------------------------------------------------------ |
| from  | Link Name        | Created in the deploy directory, pointing to the "to" path   |
| to    | Target Path      | Actual path, / prefix indicates absolute path, otherwise relative to /home/{user}/ |
| chmod | Directory Permissions (optional) |                                                              |


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

