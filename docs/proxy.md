# proxy
1. SOCKS5 proxy

```
proxy=proxy_host:proxy_port
```
1. SSH JumpHosts

config ssh jump server in ~/.jobdeploy/my.properties
```
jump_server: xxxxx
```
1. jumper server

```
jumper_server: xxxx.jumper.xxx:22
jumper_secret_prefix: 123456
jumper_secret: XXXXXXXXXXXX
sftp_prefix: /Default/xxxxxxx
```

if you don't have jumper_secret for gen totp random code,remove it 
it will prompt for input random code during each deployment,


2. jumper server sftp rules
remove sftp_prefix in my.properties
```
{
    "sftp":{
        "prod": {"pattern": "*.prod.xxxx.com", "prefix": "/Default/xxxxx"},
        "default" : {"pattern":"*", "prefix": "/Default"}
    }
}
```
