package xxx;
public class XPlugin extends BasePlugin{

    private String buildDir;
    private String srcDir;

    public void beforeTask(){
        System.out.println(deployJson);
        System.out.println("buildDir:" + buildDir + " srcDir:" + srcDir);
    }
    public void afterTask() throws Exception{
        System.out.println("after task");
        String testVar = deployJson.getString("test_var");
        System.out.println("test val " + testVar);
        System.out.println("after@@@@@@@@@");
        throw new RuntimeException("test after exception");
    }
}
