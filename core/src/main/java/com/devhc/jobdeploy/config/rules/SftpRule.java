package com.devhc.jobdeploy.config.rules;

import lombok.Data;

@Data
public class SftpRule {
    public String pattern;
    private String prefix;

}
