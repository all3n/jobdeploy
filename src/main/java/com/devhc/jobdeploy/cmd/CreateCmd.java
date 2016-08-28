package com.devhc.jobdeploy.cmd;

import com.devhc.jobdeploy.annotation.DeployCmd;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.Scanner;

/**
 *
 * @author wanghch
 *
 */
@DeployCmd(value = "c", description = "create deploy.json by template", longOpt = "create")
public class CreateCmd implements JobCmd {
  private static Logger log = LoggerFactory.getLogger("Create");
  @Autowired
  public DeployConfig deployConfig;

  @Override
  public void run() {
    try {
      create();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void create() throws IOException {
    String configFileName = Constants.DEPLOY_CONFIG_FILENAME;
    File configFile = new File(configFileName);
    Scanner scanner = new Scanner(System.in);
    if (configFile.exists()) {
      System.out.println("you are sure to create deploy.json? [y]");
      String sure = scanner.nextLine();
      if ("".equals(sure) || "y".equals(sure) || "Y".equals("sure")) {
      } else {
        log.info("cancel deploy config overwrite.");
        System.exit(0);
      }
    }

    System.out.println("do you want use interactive guide? [y]");
    String sure = scanner.nextLine();

    String serverList = "";
    String repository = "";
    String scm_username = "";
    String scm_password = "";
    if ("".equals(sure) || "y".equals(sure) || "Y".equals("sure")) {
      System.out.println("input server list split by ,:   server1,server2...");
      serverList = scanner.nextLine();
      System.out.println("input repository");
      repository = scanner.nextLine();
      if (StringUtils.isNotEmpty(repository)) {
        System.out.println("input scm_username");
        scm_username = scanner.nextLine();
        System.out.println("input scm_password");
        scm_password = scanner.nextLine();
      }
    }

    InputStream is = getClass().getClassLoader()
      .getResourceAsStream("deploy.template.json");
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = null;
    StringBuffer sb = new StringBuffer();
    while ((line = br.readLine()) != null) {
      sb.append(line);
      sb.append("\r\n");
    }

    String jsonTmpString = sb.toString();
    jsonTmpString = jsonTmpString.replace("{server}", serverList)
      .replace("{repository}", repository).replace("{scm_username}", scm_username)
      .replace("{scm_password}", scm_password);

    log.info(jsonTmpString);
    scanner.close();

    FileWriter fw = new FileWriter(configFile);
    fw.write(jsonTmpString);
    fw.close();
    log.info("generate deploy.json end.");
  }

}
