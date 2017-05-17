package com.devhc.jobdeploy.scm;

import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.exception.DeployException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public abstract class ScmDriver {

  protected String srcDir;
  protected String repositoryUrl;
  protected String branch;
  protected String tag;
  protected String revision;
  protected String commitId;

  public void init(String repositoryUrl, String srcDir) {
    this.repositoryUrl = repositoryUrl;
    this.srcDir = srcDir;
  }

  // scm action
  public abstract boolean scmExists();

  public abstract void checkout();

  public abstract void update();

  public abstract String getScmDirName();

  public abstract String getCommitId();

  public abstract void rollback(String commitid);

  public abstract List<ScmCommit> history();

  public abstract boolean isScmDriverInit();

  public String getSrcDir() {
    return srcDir;
  }

  public void setSrcDir(String srcDir) {
    this.srcDir = srcDir;
  }

  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public String getReleseDir() {
    if (StringUtils.isEmpty(branch) && StringUtils.isEmpty(tag)) {
      return Constants.REMOTE_RELEASE_DIR + "/" + getCommitId();
    } else if (StringUtils.isNotEmpty(tag)) {
      return Constants.REMOTE_TAG_DIR + "/" + tag;
    } else if (StringUtils.isNotEmpty(branch)) {
      return Constants.REMOTE_BRANCH_DIR + "/" + branch;
    } else {
      throw new DeployException("invalid release dir");
    }
  }



  public List<ScmCommit> listBranches() {
    return null;

  }

  public List<ScmCommit> listTag() {
    return null;

  }


}
