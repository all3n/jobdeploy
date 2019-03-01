package com.devhc.jobdeploy.scm.git;

import com.devhc.jobdeploy.BaseTest;
import com.devhc.jobdeploy.config.DeployJson;
import java.io.File;
import java.util.Arrays;
import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class GitScmDriverByPasswordTest extends BaseTest {

  @Autowired
  private GitScmDriver driver;
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private File testFolder;

  @Autowired
  DeployJson dc;

  public GitScmDriverByPasswordTest() {
  }

  @Before
  public void setUp() throws Exception {
    // this public project don't need password
    dc.put("scm_username", "");
    dc.put("scm_password", "");
    dc.put("scm_authtype", "");
    String repUrl = "";
    testFolder = folder.newFolder();
    driver.init(repUrl, testFolder.getPath());
    driver.checkout();
    Repository rep = driver.getGit().getRepository();
    Assert.assertEquals(repUrl,
        rep.getConfig().getString("remote", "origin", "url"));
    System.out.println("clone file:" + Arrays.asList(testFolder.list()));
  }

  @Test
  public void testGitPull() {
    driver.update();
  }

  @Test
  public void testGetCommitId() {
    System.out.println(driver.getCommitId());
  }

}
