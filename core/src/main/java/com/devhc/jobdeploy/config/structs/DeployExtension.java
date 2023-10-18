package com.devhc.jobdeploy.config.structs;

import com.devhc.jobdeploy.utils.HttpClientHelper;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class DeployExtension {
    private final Logger LOG = Loggers.get();
    private String name;
    private String url;
    private String className;
    private String md5sum;

    private ClassLoader loader;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    @Override
    public String toString() {
        return "DeployExtension{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", className='" + className + '\'' +
                ", md5sum='" + md5sum + '\'' +
                '}';
    }

    public static String calculateMD5(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return Hashing.md5().hashBytes(data).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setup() {
        if (StringUtils.isEmpty(url)) {
            return;
        }

        String deployPluginDir = System.getenv("DEPLOY_PLUGIN");
        if(deployPluginDir == null) {
            deployPluginDir = System.getenv("APPDIR") + File.separator + "exts";
        }
        String extPlugin = deployPluginDir + File.separator + "plugin_" + name + ".jar";
        File pluginFile = new File(extPlugin);
        if (pluginFile.exists()) {
            String fileMd5 = calculateMD5(pluginFile);
            if (StringUtils.isNotEmpty(fileMd5)
                    && StringUtils.isNotEmpty(md5sum)
                    && fileMd5.equals(md5sum)) {
                // plugin exists,skip
                LOG.info("{} md5sum skip", name);
                return;
            }
        }
        LOG.info("start download plugin:{}", url);
        // download plugins
        HttpClientHelper.downloadFile(url, extPlugin);
        try {
            URL jarUrl = pluginFile.toURI().toURL();
            ClassLoader cl = getClass().getClassLoader();
            loader = new URLClassLoader(new URL[]{jarUrl}, cl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
