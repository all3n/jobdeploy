package com.devhc.jobdeploy.extensions;

public interface IExtension {
    boolean hasMethod(String name);
    void afterTask() throws Exception;
    void runTask() throws Exception;
    void beforeTask() throws Exception;
}
