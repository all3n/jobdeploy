package com.devhc.jobdeploy.config.structs;

public class DeployExtension {
    private String name;
    private String url;
    private String className;
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

    @Override
    public String toString() {
        return "DeployExtension{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
