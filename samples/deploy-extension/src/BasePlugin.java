package xxx;
import org.json.JSONObject;
import javax.annotation.Resource;
public class BasePlugin{

    @Resource
    protected JSONObject deployJson;

    private String buildDir;
    private String srcDir;

    public void beforeTask(){
        System.out.println("buildDir:" + buildDir + " srcDir:" + srcDir);
    }
    public void afterTask(){
        System.out.println("after@@@@@@@@@");
    }
}
