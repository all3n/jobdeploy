package com.devhc.jobdeploy.ssh;

import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.utils.HTopGenerator;
import com.devhc.jobdeploy.utils.Loggers;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

public class JumperServerDriverTests {
    private static Logger log = Loggers.get();

    private JumperServerDriver driver;

    public void setup() throws IOException {

    }
    @Test
    @Ignore
    public void test() throws IOException, JSONException, JSchException {
        DeployJson dj = new DeployJson();
        dj.put("auth_type", "password");
        HTopGenerator g = new HTopGenerator("-");
        driver = new JumperServerDriver("-", "-");
        driver.setDeployJson(dj);
        driver.setPassword("-");
        driver.setJumperSecretPrefix("-");
        driver.setJumpGateway("-");
        driver.setJumperGatewayPort(22);
        driver.setSftpPrefix("/test/");
        driver.setCodeGenerator(g);
        driver.init();
        log.info("after init ");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
