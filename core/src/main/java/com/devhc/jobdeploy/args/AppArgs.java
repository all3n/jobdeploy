package com.devhc.jobdeploy.args;

import com.devhc.jobdeploy.config.Constants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

/**
 * deploy [headoptions] [stage]:task [taskOptions]
 *
 * @author wanghch
 */
public class AppArgs {

    private int taskId;
    private String stage;
    private String task = Constants.DEFAULT_TASK;
    private List<String> subCmds = Lists.newArrayList();

    private List<String> headOptions = Lists.newArrayList();
    private List<String> taskOptions = Lists.newArrayList();
    private Map<String, String> customOptions = Maps.newHashMap();

    public String getStage() {
        return stage;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public List<String> getHeadOptions() {
        return headOptions;
    }

    public void setHeadOptions(List<String> headOptions) {
        this.headOptions = headOptions;
    }

    public List<String> getTaskOptions() {
        return taskOptions;
    }

    public void setTaskOptions(List<String> taskOptions) {
        this.taskOptions = taskOptions;
    }

    public List<String> getSubCmds() {
        return subCmds;
    }

    public void setSubCmd(List<String> subCmds) {
        this.subCmds = subCmds;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Map<String, String> getCustomOptions() {
        return customOptions;
    }

    public void setCustomOptions(Map<String, String> customOptions) {
        this.customOptions = customOptions;
    }

    @Override
    public String toString() {
        return "AppArgs{" +
            "taskId=" + taskId +
            ", stage='" + stage + '\'' +
            ", task='" + task + '\'' +
            ", subCmds=" + subCmds +
            ", headOptions=" + headOptions +
            ", taskOptions=" + taskOptions +
            ", customOptions=" + customOptions +
            '}';
    }
}
