package com.devhc.jobdeploy.exception;

public class DeployException extends RuntimeException {

  public DeployException(String string) {
    super(string);
  }

  public DeployException(Throwable e) {
    super(e);
  }
}
