package com.devhc.jobdeploy;

import com.google.common.collect.Lists;

import java.util.List;

public class FlowManager {
  private List<String> flows = Lists.newArrayList();

  public FlowManager(List<String> flows) {
    this.flows = flows;
  }

  public List<String> getFlows() {
    return flows;
  }

  public void setFlows(List<String> flows) {
    this.flows = flows;
  }

  @Override
  public String toString() {
    return "FlowManager [flows=" + flows + "]";
  }

}
