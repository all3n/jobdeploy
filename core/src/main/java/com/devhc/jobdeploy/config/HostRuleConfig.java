package com.devhc.jobdeploy.config;


import com.devhc.jobdeploy.config.rules.SftpRule;
import lombok.Data;

import java.util.Map;


@Data
public class HostRuleConfig {
    Map<String, SftpRule> sftp;

}
