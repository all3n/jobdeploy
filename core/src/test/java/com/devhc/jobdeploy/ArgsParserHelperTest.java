package com.devhc.jobdeploy;

import static org.junit.Assert.assertEquals;

import com.devhc.jobdeploy.args.AppArgs;
import com.devhc.jobdeploy.args.ArgsParserHelper;
import java.util.Arrays;
import org.junit.Test;

public class ArgsParserHelperTest {

  @Test
  public void testParseAppArgs() {
    String line = "-a -b deploy:task -c -d";
    String args[] = line.split("\\s+");
    AppArgs appArgs = ArgsParserHelper.parseAppArgs(args);
    System.out.println(appArgs);
    assertEquals("deploy", appArgs.getStage());
    assertEquals("task", appArgs.getTask());
    assertEquals(appArgs.getHeadOptions(), Arrays.asList("-a", "-b"));
    assertEquals(appArgs.getTaskOptions(), Arrays.asList("-c", "-d"));
  }

  @Test
  public void testParseAppArgs1() {
    String line = "-a -b :task -c -d";
    String args[] = line.split("\\s+");
    AppArgs appArgs = ArgsParserHelper.parseAppArgs(args);
    System.out.println(appArgs);
    assertEquals("", appArgs.getStage());
    assertEquals("task", appArgs.getTask());
    assertEquals(appArgs.getHeadOptions(), Arrays.asList("-a", "-b"));
    assertEquals(appArgs.getTaskOptions(), Arrays.asList("-c", "-d"));

  }

  @Test
  public void testParseAppArgs_hasArgValue() {
    String line = "-a -b :task -c -d dval -e";
    String args[] = line.split("\\s+");
    AppArgs appArgs = ArgsParserHelper.parseAppArgs(args);
    System.out.println(appArgs);
    assertEquals("", appArgs.getStage());
    assertEquals("task", appArgs.getTask());
    assertEquals(appArgs.getHeadOptions(), Arrays.asList("-a", "-b"));
    assertEquals(appArgs.getTaskOptions(), Arrays.asList("-c", "-d", "dval", "-e"));

  }
}
