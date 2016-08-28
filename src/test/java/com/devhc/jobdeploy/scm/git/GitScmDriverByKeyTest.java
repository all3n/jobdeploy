package com.devhc.jobdeploy.scm.git;

import com.devhc.jobdeploy.BaseTest;
import com.devhc.jobdeploy.config.DeployJson;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Arrays;

@Ignore
public class GitScmDriverByKeyTest extends BaseTest {
  @Autowired
  private GitScmDriver driver;
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private File testFolder;

  @Autowired
  DeployJson dc;

  public GitScmDriverByKeyTest() {
  }

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    String repUrl = "";
    String testkeyfile = getClass().getClassLoader()
        .getResource("key_just4test").getFile();
    testFolder = folder.newFolder();
    dc.put("scm_keyfile", testkeyfile);
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

  @Test
  public void testGetCommit() throws NoHeadException, GitAPIException {
    RevCommit cmt = driver.getGit().log().setMaxCount(1).call().iterator().next();
    System.out.println(cmt.getCommitTime());
  }

}
