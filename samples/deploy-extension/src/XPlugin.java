package xxx;
public class XPlugin extends BasePlugin{

    private String buildDir;
    private String srcDir;

    public void beforeTask(){
        System.out.println(deployJson);
        System.out.println("buildDir:" + buildDir + " srcDir:" + srcDir);
    }
    public void afterTask(){
        System.out.println("after@@@@@@@@@");
    }
}
