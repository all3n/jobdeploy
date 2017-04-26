package com.devhc.jobdeploy.scm.svn;

import com.devhc.jobdeploy.utils.Loggers;
import org.slf4j.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;

public class SvnKitHelper {
  private static Logger log = Loggers.get();

  public static void setupLibrary() {
    DAVRepositoryFactory.setup();
    SVNRepositoryFactoryImpl.setup();
    FSRepositoryFactory.setup();
  }

  /**
   * 验证登录svn
   */
  public static SVNClientManager authSvn(String svnRoot, String username,
    String password) {
    // 初始化版本库
    setupLibrary();

    // 创建库连接
    SVNRepository repository = null;
    try {
      repository = SVNRepositoryFactory.create(SVNURL
        .parseURIEncoded(svnRoot));
    } catch (SVNException e) {
      log.error("{}", e);
      return null;
    }

    // 身份验证
    ISVNAuthenticationManager authManager = SVNWCUtil
      .createDefaultAuthenticationManager(username, password);

    // 创建身份验证管理器
    repository.setAuthenticationManager(authManager);

    DefaultSVNOptions options = (DefaultSVNOptions) SVNWCUtil
      .createDefaultOptions(true);
    SVNClientManager clientManager = SVNClientManager.newInstance(options,
      authManager);
    return clientManager;
  }

  /**
   * recursively checks out a working copy from url into wcDir
   *
   * @param clientManager
   * @param url
   *            a repository location from where a Working Copy will be
   *            checked out
   * @param revision
   *            the desired revision of the Working Copy to be checked out
   * @param destPath
   *            the local path where the Working Copy will be placed
   * @param depth
   *            checkout的深度，目录、子目录、文件
   * @return
   * @throws SVNException
   */
  public static long checkout(SVNClientManager clientManager, SVNURL url,
    SVNRevision revision, File destPath, SVNDepth depth) {
    SVNUpdateClient updateClient = clientManager.getUpdateClient();
    /*
     * sets externals not to be ignored during the checkout
     */
    updateClient.setIgnoreExternals(false);
    /*
     * returns the number of the revision at which the working copy is
     */
    try {
      return updateClient.doCheckout(url, destPath, revision, revision,
        depth, false);
    } catch (SVNException e) {
      log.error("{}", e);
    }
    return 0;
  }
}
