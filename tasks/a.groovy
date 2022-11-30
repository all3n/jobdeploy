import com.devhc.jobdeploy.DeployPlugin;
import com.devhc.jobdeploy.App;
public class Foo implements DeployPlugin{
        @Override
        public void run(App app){
                println app.getDeployJson().getName();
        }
}
