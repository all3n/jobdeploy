import com.devhc.jobdeploy.plugin.AppPlugin;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.FlowManager;
import java.util.List;
import test_script


public class MyPlugin extends AppPlugin{

  @Override
  public void onSetup(DeployJson json){
    DeployContext ctx = app.getDeployContext();
    System.out.println("on setup");
    println "test"
    def lvar = ["thing","thin2","thing3"]
    println lvar
    def script2Instance = new test_script()
    script2Instance.someMethod()
  }
  
  @Override
  public void afterRunTask(String taskName){
    DeployJson json = app.getDeployJson();
    System.out.println(json.getName());
    System.out.println(taskName);
  }
}
