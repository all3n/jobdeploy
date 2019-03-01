package com.devhc.jobdeploy.scm;

public class ScmCommit {

  public enum ScmCommitType {
    COMMIT, BRANCH, TAG
  }

  private String name;
  private String commitId;
  private String author;
  private String email;
  private String message;

  private ScmCommitType commitType;


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

  public ScmCommitType getCommitType() {
    return commitType;
  }

  public void setCommitType(ScmCommitType commitType) {
    this.commitType = commitType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ScmCommit{" +
        "name='" + name + '\'' +
        ", commitId='" + commitId + '\'' +
        ", author='" + author + '\'' +
        ", email='" + email + '\'' +
        ", message='" + message + '\'' +
        ", commitType=" + commitType +
        ", commitTime=" + commitTime +
        '}';
  }
}
