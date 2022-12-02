# Template
1. need add templates in deploy.json 
2. create templates dir in deploy stage dir
3. template use jin-java template engine
4. template var define in servers
```
{
  "servers": [
    {"server": "xxx1.xxxx.xx", "var_a": "asdfasdf"}
  ],
  "templates": [
    {"src": "a.conf", "dest": "conf/x.conf"}
  ]
}

deploy/xx/templates

a.conf

hello {{ var_a }}

```

## inner tags
1. include   (stage template dir first)
  1. stage include
  2. base include
