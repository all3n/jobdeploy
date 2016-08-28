package com.devhc.jobdeploy.scm;

public class ScmCommit {
  private String commitId;
  private String author;
  private String email;
  private String message;
  private int commitTime;

  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getCommitTime() {
    return commitTime;
  }

  public void setCommitTime(int commitTime) {
    this.commitTime = commitTime;
  }
}
