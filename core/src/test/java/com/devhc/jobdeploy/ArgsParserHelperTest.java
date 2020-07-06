package com.devhc.jobdeploy;

import static org.junit.Assert.assertEquals;

import com.devhc.jobdeploy.args.AppArgs;
import com.devhc.jobdeploy.args.ArgsParserHelper;
import java.util.Arrays;
import org.junit.Test;

public class ArgsParserHelperTest {

  @Test
  public void testParseAppArgs() {
    String line = "deploy:task -c -d";
    String args[] = line.split("\\s+");
    AppArgs appArgs = ArgsParserHelper.parseAppArgs(args);
    System.out.println(appArgs);
    assertEquals("deploy", appArgs.getStage());
    assertEquals("task", appArgs.getTask());
    assertEquals(appArgs.getTaskOptions(), Arrays.asList("-c", "-d"));
  }

  @Test
  public void testParseAppArgs1() {
    String line = "-h :task -c -d";
    String args[] = line.split("\\s+");
    AppArgs appArgs = ArgsParserHelper.parseAppArgs(args);
    System.out.println(appArgs);
    assertEquals("", appArgs.getStage());
    assertEquals("task", appArgs.getTask());
    assertEquals(appArgs.getHeadOptions(), Arrays.asList("-h"));
    assertEquals(appArgs.getTaskOptions(), Arrays.asList("-c", "-d"));

  }

  @Test
  public void testParseAppArgs_hasArgValue() {
    String line = ":task -c -d dval -e";
    String args[] = line.split("\\s+");
    AppArgs appArgs = ArgsParserHelper.parseAppArgs(args);
    System.out.println(appArgs);
    assertEquals("", appArgs.getStage());
    assertEquals("task", appArgs.getTask());
    assertEquals(appArgs.getTaskOptions(), Arrays.asList("-c", "-d", "dval", "-e"));

  }
}
