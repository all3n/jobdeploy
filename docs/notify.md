# Notify

```
"alerts": ["alert_xx"],
"alert_xx": {
      "type": "http",
      "url" : "http://xxx.alert.xxx:3333/alarm/api",
      "method" : "post",
      "params" : {
        "groups": "alert",
        "content": "{msg}",
      },
      "auth"   : "user:token",
      "headers": {"charset" : "UTF-8"}
  }
}



```
