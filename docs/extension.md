# Extension

add extensions in deploy json

```
  "extensions" : {
      "build": {
          "class":"xxx.XPlugin"
      }
  },
```

add extensions jar in ext dir

## ext search dirs
1. JOB_DEPLOY_HOME/exts
2. DEPLOY_PLUGIN environment variable



## examples
1. plugins
add you jars in ext dirs like this
```
exts:
  - plugin_a.jar
  - plugin_b.jar
```

1. single class (just for example)
```
package xxx;
import org.json.JSONObject;
import javax.annotation.Resource;
public class XPlugin {

    @Resource
    private JSONObject deployJson;
    private String buildDir;
    private String srcDir;

    public void beforeTask(){
        System.out.println("buildDir:" + buildDir + " srcDir:" + srcDir);
    }
    public void runTask(){
        System.out.println("deployJson:" + deployJson);
    }
    public void afterTask(){
        System.out.println("after@@@@@@@@@");
    }
}
```
then compile class to exts dir:
javac -d ~/opt/jobdeploy/exts XPlugin.java -cp ~/.m2/repository/org/json/json/20230227/json-20230227.jar

then ~/opt/jobdeploy/exts dir:
  xxx/XPlugin.class

the extension will call when method defined in class
  1. beforeTask
  1. afterTask
  1. runTask

field use @Resource support Inject by spring
string filed inject support:
   buildDir
   srcDir

