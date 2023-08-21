package com.devhc.jobdeploy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import javax.annotation.PostConstruct;

import com.devhc.jobdeploy.utils.CliHelper;
import com.devhc.jobdeploy.utils.Loggers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Created by wanghch on 16/8/26.
 */
@Component
public class DeployCustomConfig {
    private static Logger LOG = Loggers.get();
    private File customConfigDir;
    private File customConfigFile;

    private File hostRuleFile;

    private Properties customProp = null;
    private HostRuleConfig hostRule;


    @PostConstruct
    public void init() {
        String currentUserHome = System.getProperty("user.home");
        this.customConfigDir = new File(currentUserHome + File.separator + Constants.CUSTOM_CONFIG_DIR);
        this.customConfigFile = new File(
                customConfigDir + File.separator + Constants.CUSTOM_CONFIG_FILE);
        this.hostRuleFile = new File(
                customConfigDir + File.separator + Constants.HOSTS_ROLE_FILE);
        if (isExists()) {
            InputStreamReader is = null;
            try {
                customProp = new Properties();
                is = new InputStreamReader(Files.newInputStream(customConfigFile.toPath()),
                        "UTF-8");
                customProp.load(is);
            } catch (IOException e) {
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        if(hostRuleFile.exists()){
            ObjectMapper om = new ObjectMapper();
            try {
                this.hostRule = om.readValue(hostRuleFile, HostRuleConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isBase64Str(String base64Str) {
        return Base64.isBase64(base64Str);

    }

    public String getCustomConfig(String key) {
        if (customProp != null) {
            String out = customProp.getProperty(key);
            if (key.contains("password")) {
                if (out == null) {
                    out = CliHelper.askPassword();
                    try {
                        setCustomConfig(key, Base64.encodeBase64String(out.getBytes(StandardCharsets.UTF_8)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (isBase64Str(out)) {
                    return new String(Base64.decodeBase64(out), StandardCharsets.UTF_8);
                } else {
                    String base64pwd = Base64.encodeBase64String(out.getBytes(StandardCharsets.UTF_8));
                    try {
                        LOG.info("{} encoding for config", key);
                        setCustomConfig(key, base64pwd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return out;
        }
        return null;
    }

    public void setCustomConfig(String name, String value) throws IOException {
        if (customProp == null && !isExists()) {
            customConfigDir.mkdirs();
            this.customConfigFile = new File(
                    customConfigDir + File.separator + Constants.CUSTOM_CONFIG_FILE);
            customProp = new Properties();
            if (customConfigFile.isFile()) {
                InputStreamReader isr = null;
                isr = new InputStreamReader(new FileInputStream(customConfigFile), "UTF-8");
                customProp.load(isr);
                IOUtils.closeQuietly(isr);
            }
        }
        if (name.contains("password") && !isBase64Str(value)) {
            value = Base64.encodeBase64String(value.getBytes(StandardCharsets.UTF_8));
        }
        customProp.put(name, value);
        LOG.info("update custom {}", name);
        storeToFile();
    }

    public boolean isExists() {
        return customConfigDir.exists();
    }

    public void storeToFile() throws IOException {
        if (customProp == null) {
            return;
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(customConfigFile),
                "UTF-8");
        customProp.store(osw, "custom config of jobdeploy");
        IOUtils.closeQuietly(osw);
    }

    public HostRuleConfig getHostRule() {
        return hostRule;
    }
}
