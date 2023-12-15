# GroovyScript support
1. load deploy.groovy in deploy directory

```java
import com.devhc.jobdeploy.plugin.AppPlugin;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.FlowManager;
import java.util.List;

public class MyPlugin extends AppPlugin{

  @Override
  public void onSetup(DeployJson json){
    DeployContext ctx = app.getDeployContext();
    FlowManager flowMgr = ctx.getFlowManager();
    List<String> flows = flowMgr.getFlows();
    System.out.println(flows);
    System.out.println("on setup");
  }
  
  @Override
  public void afterRunTask(String taskName){
    DeployJson json = app.getDeployJson();
    System.out.println(json.getName());
    System.out.println(taskName);
  }
}
```
