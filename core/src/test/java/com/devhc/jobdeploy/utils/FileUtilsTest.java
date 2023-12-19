package com.devhc.jobdeploy.utils;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.junit.Assert;
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

  @Test
  public void testFileModel(){
    Assert.assertEquals(0777, FileUtils.translatePosixPermissionToMode(PosixFilePermissions.fromString("rwxrwxrwx")));
    Assert.assertEquals(0755, FileUtils.translatePosixPermissionToMode(PosixFilePermissions.fromString("rwxr-xr-x")));
    Assert.assertEquals(0644, FileUtils.translatePosixPermissionToMode(PosixFilePermissions.fromString("rw-r--r--")));
    Assert.assertEquals(0600, FileUtils.translatePosixPermissionToMode(PosixFilePermissions.fromString("rw-------")));


  }

}
