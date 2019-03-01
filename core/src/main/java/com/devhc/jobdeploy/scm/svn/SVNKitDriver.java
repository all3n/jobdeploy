package com.devhc.jobdeploy.scm.svn;

import com.devhc.jobdeploy.svn.structs.SVNDiffHistoryLog;
import com.devhc.jobdeploy.utils.Loggers;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SVNKitDriver {

  private String svnroot;
  private String username;
  private String password;
  private SVNURL repositoryUrl;
  private SVNRepository repository;
  private SVNClientManager clientManager;

  public Logger log = Loggers.get();

  static {
    SVNRepositoryFactoryImpl.setup();
  }

  public SVNKitDriver(String svnroot, String username, String password) {
    this.svnroot = svnroot;
    this.username = username;
    this.password = password;
    try {
      this.init();
    } catch (SVNException e) {
      log.error("init svn error", e);
    }
  }

  private void init() throws SVNException {
    repositoryUrl = SVNURL.parseURIEncoded(svnroot);
    repository = SVNRepositoryFactory.create(repositoryUrl);
    ISVNAuthenticationManager authManager = SVNWCUtil
        .createDefaultAuthenticationManager(username, password);
    repository.setAuthenticationManager(authManager);

    DefaultSVNOptions options = (DefaultSVNOptions) SVNWCUtil
        .createDefaultOptions(true);
    clientManager = SVNClientManager.newInstance(options, authManager);
    clientManager.getUpdateClient().setEventHandler(new UpdateEventHandler());
  }

  public long getLastestVersion() {
    try {
      return repository.getLatestRevision();
    } catch (SVNException e) {
      log.error(e.getMessage());
    }
    return 0;
  }

  public void ListDir(String url) {
    try {
      Collection entries = repository.getDir(url, -1, null,
          (Collection) null);
      Iterator<SVNDirEntry> iter = entries.iterator();
      while (iter.hasNext()) {
        SVNDirEntry entry = iter.next();
        String entryPath = (url.equals("")) ? entry.getName() : url
            + "/" + entry.getName();
        System.out.println(entryPath);
        if (entry.getKind() == SVNNodeKind.DIR) {
          ListDir(entryPath);
        }
      }

    } catch (SVNException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<SVNDiffHistoryLog> getLastestUpdateHistory(int startRevision, int endRevision) {
    ArrayList<SVNDiffHistoryLog> logList = new ArrayList<SVNDiffHistoryLog>();
    try {
      Collection<SVNLogEntry> history = repository.log(
          new String[]{""}, null, startRevision, endRevision,
          true, true);
      Iterator<SVNLogEntry> iter = history.iterator();
      while (iter.hasNext()) {
        SVNLogEntry entry = iter.next();
        SVNDiffHistoryLog diffLog = new SVNDiffHistoryLog(entry);
        logList.add(diffLog);
      }

    } catch (SVNException e) {
      e.printStackTrace();
    }
    return logList;
  }

  public void checkout(String url, File destFile, SVNRevision revision) throws SVNException {
    SVNUpdateClient updateClient = clientManager.getUpdateClient();
    SVNLoggerAdapter log = new SVNLoggerAdapter();
    updateClient.setIgnoreExternals(true);
    SVNURL checkUrl = repositoryUrl.appendPath(url, false);
    updateClient
        .doCheckout(checkUrl, destFile, revision, revision, SVNDepth.fromRecurse(true), true);

  }

  public void update(File file, SVNRevision revision, boolean recursive) throws SVNException {
    SVNUpdateClient updateClient = clientManager.getUpdateClient();
    updateClient.setIgnoreExternals(true);
    updateClient.doUpdate(file, revision, SVNDepth.fromRecurse(recursive), false, false);
  }

  public void checkOrUpdate(String target) {
    File targetDest = new File(target);
    System.out.println(targetDest);
    boolean showCheckout = false;
    if (targetDest.exists()) {
      String svnFiles[] = targetDest.list(new FilenameFilter() {

        public boolean accept(File dir, String name) {
          if (name.contains(".svn")) {
            return true;
          }
          return false;
        }
      });
      System.out.println(Arrays.asList(svnFiles));

      if (svnFiles.length == 0) {
        showCheckout = true;
      }

    } else {
      targetDest.mkdirs();
      showCheckout = true;
    }

    try {
      if (showCheckout) {
        checkout("", targetDest, SVNRevision.HEAD);
      } else {
        update(targetDest, SVNRevision.HEAD, true);
      }
    } catch (SVNException e) {
      e.printStackTrace();
    }
  }

  public String getSvnroot() {
    return svnroot;
  }

  public void setSvnroot(String svnroot) {
    this.svnroot = svnroot;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
