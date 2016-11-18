package com.devhc.jobdeploy.event;

import java.util.EventListener;

/**
 * Created by wanghch on 16/11/17.
 */
public interface DeployAppEventListener extends EventListener{
  public void onAppStart(DeployAppEvent data);

  public void onTaskStart(DeployAppTaskEvent data);

  public void onTaskEnd(DeployAppTaskEvent data);

  public void onAppEnd(DeployAppEvent data);
}
