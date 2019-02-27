package com.devhc.jobdeploy.utils;

import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class DeployUtilsTest {

  private String test;

  @Test
  public void testParseVars() {
    String rawStr = "test_${fasd}_${fff}";
    Set<String> vars = DeployUtils.parseVars(rawStr);
    Assert.assertEquals(Sets.newHashSet("fasd", "fff"), vars);
  }

  @Test
  public void testparseRealValue() {
    String rawStr = "${test}asdf${asdf}";
    DeployUtilsTest obj = new DeployUtilsTest();
    obj.setTest("hhh");
    String ret = DeployUtils.parseRealValue(rawStr, obj);
    System.out.println(ret);
  }

  public String getTest() {
    return test;
  }

  public void setTest(String test) {
    this.test = test;
  }

}
