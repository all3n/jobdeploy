package com.devhc.jobdeploy;

import com.devhc.jobdeploy.args.AppArgs;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.manager.CompressManager;
import com.devhc.jobdeploy.scm.ScmDriver;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeployContext {

    private ExecMode execMode = ExecMode.CLI;

    private AppArgs appArgs;
    @Option(name = "-h", usage = "print help usage", aliases = "--help")
    public boolean help;

    @Option(name = "-l", usage = "print task list", aliases = "--list")
    public boolean list;

    @Option(name = "-V", usage = "print verbose for debug error stacktrace", aliases = "--verbose")
    public boolean verbose = false;

    @Option(name = "-t", usage = "specify a host", aliases = "--hosts")
    public String hosts;

    @Option(name = "-y", usage = "default yes to deploy", aliases = "--yes")
    public boolean yes;

    @Option(name = "-v", usage = "print deploy version", aliases = "--version")
    public boolean version;

    private boolean tmpDirCreate = false;
    /**
     * 1. tempDir if deployMode is latest 2. . if deployMode is local
     */
    private String buildDir;
    /**
     * src dir
     */
    private String srcDir;

    /**
     * use timetamp as deployid
     */
    private String deployid;

    private ScmDriver scmDriver;

    private boolean uploadJob;

    private long deployTimestamp;

    @Autowired
    CompressManager compressManager;

    @Autowired
    DeployJson deployJson;

    @Autowired
    FlowManager flowManager;

    public String getBuildDir() {
        return buildDir;
    }

    public void setBuildDir(String buildDir) {
        this.buildDir = buildDir;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    public String getDeployid() {
        return deployid;
    }

    public void setDeployid(String deployid) {
        this.deployid = deployid;
    }

    public ScmDriver getScmDriver() {
        return scmDriver;
    }

    public void setScmDriver(ScmDriver scmDriver) {
        this.scmDriver = scmDriver;
    }

    public AppArgs getAppArgs() {
        return appArgs;
    }

    public void setAppArgs(AppArgs appArgs) {
        this.appArgs = appArgs;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isUploadJob() {
        return uploadJob;
    }

    public void setUploadJob(boolean uploadJob) {
        this.uploadJob = uploadJob;
    }

    public boolean isTmpDirCreate() {
        return tmpDirCreate;
    }

    public String getRemoteTmp() throws Exception {
        if (!tmpDirCreate) {
            deployJson.getDeployServers().mkdirDeployTmpDir();
            tmpDirCreate = true;
        }
        return deployJson.getRemoteTmpUserDir();
    }

    public CompressManager getCompressManager() {
        return compressManager;
    }

    public void setCompressManager(CompressManager compressManager) {
        this.compressManager = compressManager;
    }

    public long getDeployTimestamp() {
        return deployTimestamp;
    }

    public void setDeployTimestamp(long deployTimestamp) {
        this.deployTimestamp = deployTimestamp;
    }

    public String getReleseDir() {
        if (deployJson.getDeployMode() == DeployMode.LOCAL) {
            return Constants.REMOTE_TIMESTAMP_DIR + "/" + getDeployid();
        } else if (deployJson.getDeployMode() == DeployMode.LATEST) {
            return scmDriver.getReleseDir();
        } else {
            throw new DeployException("deploy mode invalid");
        }
    }

    public ExecMode getExecMode() {
        return execMode;
    }

    public void setExecMode(ExecMode execMode) {
        this.execMode = execMode;
    }

    public FlowManager getFlowManager() {
        return flowManager;
    }

    public void setFlowManager(FlowManager flowManager) {
        this.flowManager = flowManager;
    }


    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }
}
