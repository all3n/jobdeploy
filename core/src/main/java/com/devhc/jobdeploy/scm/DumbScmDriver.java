package com.devhc.jobdeploy.scm;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class DumbScmDriver extends ScmDriver{

  @Override
  public boolean scmExists() {
    return true;
  }

  @Override
  public void checkout() {

  }

  @Override
  public void update() {

  }

  @Override
  public String getScmDirName() {
    return null;
  }

  @Override
  public String getCommitId() {
    return "commitid";
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
    return Lists.newArrayList();
  }

  @Override
  public boolean isScmDriverInit() {
    return false;
  }
}
