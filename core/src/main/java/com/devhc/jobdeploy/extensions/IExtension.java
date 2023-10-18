package com.devhc.jobdeploy.extensions;

public interface IExtension {
    boolean hasMethod(String name);
    void afterTask();
    void runTask();
    void beforeTask();
}
