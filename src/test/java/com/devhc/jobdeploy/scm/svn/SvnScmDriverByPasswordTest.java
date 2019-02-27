package com.devhc.jobdeploy.scm.svn;

import com.devhc.jobdeploy.BaseTest;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.scm.git.GitScmDriver;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;

@Ignore
public class SvnScmDriverByPasswordTest extends BaseTest {

  @Autowired
  private GitScmDriver driver;
  public File folder = Files.createTempDir();

  @Autowired
  DeployJson dc;
  private SVNKitDriver svn;

  public SvnScmDriverByPasswordTest() {
  }

  @Before
  public void setUp() throws Exception {
    String scmUserName = "";
    String scmPassword = "";

    this.svn = new SVNKitDriver("", scmUserName, scmPassword);
  }

  @Test
  public void testCheckout() {
    try {
      svn.checkout("", folder, SVNRevision.HEAD);
    } catch (SVNException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testVersion() {
    System.out.println(svn.getLastestVersion());
  }
}
