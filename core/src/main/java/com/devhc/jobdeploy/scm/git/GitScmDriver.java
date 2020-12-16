package com.devhc.jobdeploy.scm.git;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.DeployMode;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.scm.ScmCommit;
import com.devhc.jobdeploy.scm.ScmCommit.ScmCommitType;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class GitScmDriver extends ScmDriver {

  private static Logger log = Loggers.get();
  @Autowired
  private DeploySShSessionFactory sshFactory;
  @Autowired
  private DeployJson dc;
  private Git git;
  private File srcFile;

  @Autowired
  private App app;

  @Autowired
  DeployContext deployContext;

  @Override
  public void init(String repositoryUrl, String srcDir) {
    super.init(repositoryUrl, srcDir);
    String userDir = System.getProperty("user.dir");
    if (StringUtils.isEmpty(repositoryUrl)) {
      if (new File(userDir + "/.git").exists()) {
      } else if (new File(userDir + "/../.git").exists()) {
        userDir += "/../";
      } else {
        throw new DeployException("local git is not set");
      }
      try {
        Git localGit = Git.open(new File(userDir));
        String remoteOriginUrl = localGit.getRepository().getConfig()
            .getString("remote", "origin", "url");
        log.info("repository is not set,use deploy repository Url:{}", remoteOriginUrl);
        this.repositoryUrl = remoteOriginUrl;
      } catch (IOException e) {
        throw new DeployException(e);
      }
    }

    if (dc.getDeployMode() == DeployMode.LOCAL) {
      if (new File(srcDir + "/../.git").exists()) {
        srcDir = srcDir + "/../";
      }
    }
    srcFile = new File(srcDir);
  }

  /**
   * 在需要git初始化操作前加上
   */
  public void ensureGitInit() {
    if (git == null) {
      try {
        if (scmExists()) {
          git = Git.open(srcFile);

        }
      } catch (IOException e) {
        throw new DeployException(e.getMessage());
      }
    }
  }

  @PreDestroy
  public void destory() {
    if (git != null) {
      git.close();
    }
  }

  @Override
  public void checkout() {
    CloneCommand cmd = Git.cloneRepository();
    try {
      cmd.setURI(getRepositoryUrl());
      cmd.setDirectory(srcFile);
      cmd.setProgressMonitor(new TextProgressMonitor(new DeployLogPrintWriter(app)));
      String branch = "master";
      if (StringUtils.isNotEmpty(getBranch())) {
        branch = getBranch();
      } else if (StringUtils.isNotEmpty(getTag())) {
        branch = getTag();
      } else if (StringUtils.isNotEmpty(dc.getBranch())) {
        branch = dc.getBranch();
      }

      log.info("git checkout {}", branch);
      cmd.setBranch(branch);
      cmd.setCloneSubmodules(true);
      setCmdAuth(cmd);
      git = cmd.call();

      if (StringUtils.isNotEmpty(getRevision())) {
        log.info("git reset --hard {}", getRevision());
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(getRevision()).call();
      }

    } catch (Exception e) {
      throw new DeployException(e);
    }

  }

  protected TransportCommand setCmdAuth(TransportCommand cmd) {
    log.debug("scmAuthType:{} ,{},{}", dc.getScmAuthtype(),
        dc.getScmKeyFile(), dc.getScmKeyFilePass());
    if (dc.getScmAuthtype().equals("key")) {
      sshFactory.setSshKeyFilePath(dc.getScmKeyFile());
      sshFactory.setSshKeyPassword(dc.getScmKeyFilePass());
      cmd.setTransportConfigCallback(new TransportConfigCallback() {
        @Override
        public void configure(Transport transport) {
          SshTransport sshTransport = (SshTransport) transport;
          sshTransport.setSshSessionFactory(sshFactory);
        }
      });
    } else if (dc.getScmAuthtype().equals("password")) {
      log.info("{},{}", dc.getScmUsername(), dc.getScmPassword());
      cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
          dc.getScmUsername(), dc.getScmPassword()));
    } else {
      throw new DeployException("unsupport git auth type:"
          + dc.getScmAuthtype());
    }
    return cmd;
  }

  public List<String> listLocalBranch() {
    ensureGitInit();
    List<String> refNameList = Lists.newArrayList();
    ListBranchCommand lbc = git.branchList();
    List<Ref> refList;
    try {
      refList = lbc.call();
      for (Ref ref : refList) {
        // log.info("isSymbolic:{} getObjectId:{} getName:{} getLeaf:{} getPeeledObjectId:{} getStorage:{}",
        // ref.isSymbolic(), ref.getObjectId(), ref.getName(), ref.getLeaf(), ref.getPeeledObjectId(),
        // ref.getStorage());
        refNameList.add(ref.getTarget().getName());
      }
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return refNameList;
  }

  /**
   * @return
   */
  public List<String> listRemoteBranch() {
    ensureGitInit();
    List<String> refNameList = Lists.newArrayList();
    ListBranchCommand lbc = git.branchList();
    List<Ref> refList;
    try {
      lbc.setListMode(ListMode.REMOTE);
      refList = lbc.call();
      /**
       * remotes/origin/v1
       * remotes/origin/HEAD -> origin/master
       * remotes/origin/master
       */
      for (Ref ref : refList) {
        // log.info("isSymbolic:{} getObjectId:{} getName:{} getLeaf:{} getPeeledObjectId:{} getStorage:{}",
        // ref.isSymbolic(), ref.getObjectId(), ref.getName(), ref.getLeaf(), ref.getPeeledObjectId(),
        // ref.getStorage());

        // skip remote HEAD symbol link
        if (!ref.isSymbolic()) {
          refNameList.add(ref.getLeaf().getName());
        }
      }
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return refNameList;
  }

  @Override
  public void update() {
    ensureGitInit();
    try {
      String localBranch = git.getRepository().getBranch();
      List<String> refNameList = listLocalBranch();
      List<String> remoteRefNameList = listRemoteBranch();

      String branch = "master";
      if (StringUtils.isNotEmpty(getBranch())) {
        branch = getBranch();
      } else if (StringUtils.isNotEmpty(getTag())) {
        branch = getTag();
      } else if (StringUtils.isNotEmpty(dc.getBranch())) {
        branch = dc.getBranch();
      }
      log.info("branch:{} deployMode:{}", localBranch, dc.getDeployMode());
      if (!localBranch.equals(branch) && dc.getDeployMode() != DeployMode.LOCAL) {
        if (refNameList.contains("refs/heads/" + branch)) {
          CheckoutCommand cm = git.checkout();
          cm.setName("refs/heads/" + branch);
          cm.call();
        } else if (remoteRefNameList.contains("refs/remotes/origin/" + branch)) {
          CheckoutCommand cm = git.checkout();
          cm.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
          cm.setCreateBranch(true);
          cm.setName(branch);
          cm.call();
        }
        log.info("switch {} to {}", localBranch,
            branch);
      }

      PullCommand pullCmd = git.pull();
      pullCmd.setProgressMonitor(new TextProgressMonitor());
      PullResult res = ((PullCommand) setCmdAuth(pullCmd)).call();
      FetchResult fres = res.getFetchResult();
      log.info("fetch:{}", fres.getMessages());
      log.info("update:{}", res.getMergeResult().getMergeStatus().toString());
    } catch (Exception e) {
      throw new DeployException(e.getMessage());
    }
  }

  @Override
  public String getScmDirName() {
    return ".git";
  }

  @Override
  public String getCommitId() {
    ensureGitInit();
    if (StringUtils.isNotEmpty(revision)) {
      return revision;
    }
    if (StringUtils.isNotEmpty(this.commitId)) {
      return this.commitId;
    }
    String commitId = null;
    try {
      Iterable<RevCommit> iter = git.log().setMaxCount(1).setSkip(0)
          .call();
      Iterator<RevCommit> it = iter.iterator();
      if (it.hasNext()) {
        commitId = it.next().getId().getName();
      } else {
        throw new DeployException("git respository has no commitid");
      }
    } catch (Exception e) {
      throw new DeployException(e.getMessage());
    }
    this.commitId = commitId;
    return commitId;
  }

  @Override
  public boolean checkScmDirValid() {
    ensureGitInit();
    String remoteOriginUrl = git.getRepository().getConfig()
        .getString("remote", "origin", "url");
    if (StringUtils.isNotEmpty(repositoryUrl)
        && !remoteOriginUrl.equals(repositoryUrl)) {
      log.warn("repositoryUrl is not match \n"
          + repositoryUrl + "\n" + remoteOriginUrl);
      return false;
    }
    return true;
  }

  public Git getGit() {
    return git;
  }

  public void setGit(Git git) {
    this.git = git;
  }

  @Override
  public void rollback(String commitid) {

  }

  @Override
  public boolean scmExists() {
    String gitFile[] = srcFile.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.equals(".git");
      }
    });
    if (gitFile != null && gitFile.length > 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<ScmCommit> history() {
    ensureGitInit();
    List<ScmCommit> ret = Lists.newArrayList();
    try {
      Iterable<RevCommit> commits = git.log().setMaxCount(10).call();
      for (RevCommit commit : commits) {
        ScmCommit c = new ScmCommit();
        c.setAuthor(commit.getAuthorIdent().getName());
        c.setEmail(commit.getAuthorIdent().getEmailAddress());
        c.setCommitTime(commit.getCommitTime());
        c.setCommitType(ScmCommitType.COMMIT);
        c.setCommitId(commit.getName());
        c.setMessage(commit.getFullMessage());
        ret.add(c);
      }
    } catch (Exception e) {
      throw new DeployException(e.getMessage());
    }
    return ret;
  }

  @Override
  public boolean isScmDriverInit() {
    return git != null;
  }


  @Override
  public List<ScmCommit> listBranches() {
    ensureGitInit();
    List<ScmCommit> branchCommit = Lists.newArrayList();
    try {
      List<Ref> branchList = git.branchList().setListMode(ListMode.REMOTE).call();
      for (Ref branchRef : branchList) {
        ScmCommit scmCommit = new ScmCommit();
        scmCommit.setCommitId(ObjectId.toString(branchRef.getObjectId()));
        scmCommit.setName(branchRef.getName());
        scmCommit.setCommitType(ScmCommitType.BRANCH);

        branchCommit.add(scmCommit);
      }
    } catch (GitAPIException e) {
      throw new DeployException(e);
    }
    return branchCommit;
  }

  @Override
  public List<ScmCommit> listTag() {
    ensureGitInit();
    List<ScmCommit> tagCommit = Lists.newArrayList();
    try {
      List<Ref> tagList = git.tagList().call();
      for (Ref tagRef : tagList) {
        ScmCommit scmCommit = new ScmCommit();
        scmCommit.setCommitId(ObjectId.toString(tagRef.getObjectId()));
        scmCommit.setName(tagRef.getName());

        scmCommit.setCommitType(ScmCommitType.TAG);
        tagCommit.add(scmCommit);
      }
    } catch (GitAPIException e) {
      throw new DeployException(e);
    }
    return tagCommit;
  }
}
