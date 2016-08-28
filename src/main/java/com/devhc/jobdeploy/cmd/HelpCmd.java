package com.devhc.jobdeploy.cmd;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.annotation.DeployCmd;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import org.apache.commons.cli.HelpFormatter;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.annotation.Autowired;

@DeployCmd(value = "h", description = "help", longOpt = "help")
public class HelpCmd implements JobCmd {
  @Autowired
  App app;

  @Override
  public void run() {
    HelpFormatter formatter = new HelpFormatter();
    CmdLineParser headOptionParser = new CmdLineParser(app.getDeployContext());
    System.out.println(AnsiColorBuilder.red("HeaderOptions:"));
    headOptionParser.printUsage(System.out);
  }

}
