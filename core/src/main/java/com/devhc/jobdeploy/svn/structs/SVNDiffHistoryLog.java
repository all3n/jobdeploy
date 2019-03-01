package com.devhc.jobdeploy.svn.structs;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

public class SVNDiffHistoryLog {

  private long revision;
  private Date date;
  private String message;
  private String author;
  private int issueId = 0;
  private int reviewId = 0;
  private ArrayList<String> changeLog;

  public SVNDiffHistoryLog(SVNLogEntry entry) {

    this.revision = entry.getRevision();
    this.date = entry.getDate();
    this.message = entry.getMessage();
    String issuePattern = "\\[(.+)\\]\\[r(\\d+)\\]\\[i(\\d+)\\].+";
    Pattern p = Pattern.compile(issuePattern);
    Matcher m = p.matcher(message);

    if (m.matches() && m.groupCount() == 3) {
      try {
        reviewId = Integer.parseInt(m.group(2));
        issueId = Integer.parseInt(m.group(3));
      } catch (NumberFormatException e) {
      }
    }

    this.author = entry.getAuthor();
    this.changeLog = new ArrayList<String>();
    if (entry.getChangedPaths().size() > 0) {
      Set<String> changedPathsSet = entry.getChangedPaths().keySet();

      for (Iterator<String> changedPaths = changedPathsSet.iterator(); changedPaths
          .hasNext(); ) {
        SVNLogEntryPath entryPath = entry.getChangedPaths().get(changedPaths.next());
        changeLog.add(" " + entryPath.getType() + " " + entryPath.getPath() +
            ((entryPath.getCopyPath() != null)
                ? " (from " + entryPath.getCopyPath() + " revision " + entryPath.getCopyRevision()
                + ")" : "")
            + "<br />");
      }
    }
  }

  public String getFormatLog() {
    StringBuffer sb = new StringBuffer();
    sb.append("revision: " + this.getRevision() + "<br />");
    sb.append("author: " + this.getAuthor() + "<br />");
    sb.append("date: " + this.getDate() + "<br />");
    sb.append("log message: " + this.getMessage() + "<br />");
    if (changeLog.size() > 0) {
      sb.append("<br />");
      sb.append("changed paths:<br />");
      Iterator<String> iter = changeLog.iterator();
      while (iter.hasNext()) {
        sb.append(iter.next());
      }
    }
    return sb.toString();
  }

  public long getRevision() {
    return revision;
  }

  public Date getDate() {
    return date;
  }

  public String getMessage() {
    return message;
  }

  public String getAuthor() {
    return author;
  }

  public ArrayList<String> getChangeLog() {
    return changeLog;
  }

  public int getIssueId() {
    return issueId;
  }

  public int getReviewId() {
    return reviewId;
  }

}
