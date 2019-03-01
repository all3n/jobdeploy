package com.devhc.jobdeploy.utils;

import org.junit.Before;
import org.junit.Test;

public class FileUtilsTest {

  public FileUtilsTest() {
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGetDeployTmpDir() {
    String tmpDir = FileUtils.getDeployTmpDir("deploy");
    System.out.println(tmpDir);
  }

}
