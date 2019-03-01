package com.devhc.jobdeploy.scm.svn;

import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.scm.ScmCommit;
import com.devhc.jobdeploy.scm.ScmDriver;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;

@Lazy
@Component
public class SvnScmDriver extends ScmDriver {

  private SVNKitDriver svnkit;

  @Autowired
  private DeployJson dc;
  @Autowired
  DeployContext deployContext;
  File srcFile;
  private String svnpath;

  @Override
  public void init(String repositoryUrl, String srcDir) {
    super.init(repositoryUrl, srcDir);
    srcFile = new File(srcDir);
    String scmPath = getScmPath();
    if (StringUtils.isEmpty(scmPath)) {
      svnpath = repositoryUrl;
    } else {
      if (scmPath.startsWith("http")) {
        svnpath = scmPath;
      } else {
        // get url base from trunk url
        try {
          URL url = new URL(repositoryUrl);
          String urlBase = url.getProtocol() + "://" + url.getHost();
          svnpath = urlBase + (scmPath.startsWith("/") ? "" : "/") + scmPath;
        } catch (Exception e) {
          throw new DeployException(e);
        }
      }
    }
    svnkit = new SVNKitDriver(svnpath, dc.getScmUsername(), dc.getScmPassword());
  }

  public String getScmPath() {
    String scmPath = "";
    if (StringUtils.isNotEmpty(getBranch())) {
      branch = getBranch();
      scmPath = dc.getSvnBranchPath().replace("{branch}", branch);
    } else if (StringUtils.isNotEmpty(getTag())) {
      branch = getTag();
      scmPath = dc.getSvnTagPath().replace("{tag}", branch);
    } else if (StringUtils.isNotEmpty(dc.getBranch())) {
      branch = dc.getBranch();
      scmPath = dc.getSvnBranchPath().replace("{branch}", branch);
    }
    return scmPath;
  }

  @Override
  public boolean scmExists() {
    String scmFile[] = srcFile.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.equals(".svn");
      }
    });
    if (scmFile != null && scmFile.length > 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void checkout() {
    try {
      String revision = getRevision();
      SVNRevision rev = SVNRevision.HEAD;
      if (StringUtils.isNotEmpty(revision)) {
        rev = SVNRevision.parse(revision);
      }

      svnkit.checkout("", srcFile, rev);
    } catch (SVNException e) {
      throw new DeployException(e);
    }
  }

  @Override
  public void update() {
    try {
      svnkit.update(srcFile, SVNRevision.HEAD, true);
    } catch (SVNException e) {
      throw new DeployException(e);
    }
  }

  @Override
  public String getScmDirName() {
    return ".svn";
  }

  @Override
  public String getCommitId() {
    if (StringUtils.isNotEmpty(revision)) {
      return revision;
    }
    if (this.commitId == null) {
      commitId = String.valueOf(svnkit.getLastestVersion());
    }
    return commitId;
  }

  @Override
  public boolean checkScmDirValid() {
    return true;
  }

  @Override
  public void rollback(String commitid) {
  }

  @Override
  public List<ScmCommit> history() {
    return null;
  }

  @Override
  public boolean isScmDriverInit() {
    return svnkit != null;
  }

}
