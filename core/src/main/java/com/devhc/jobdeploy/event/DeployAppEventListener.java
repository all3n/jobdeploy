package com.devhc.jobdeploy.event;

import java.util.EventListener;

/**
 * Created by wanghch on 16/11/17.
 */
public interface DeployAppEventListener extends EventListener {

  public void onAppStart(DeployAppEvent event);

  public void onTaskStart(DeployAppTaskEvent event);

  public void onTaskEnd(DeployAppTaskEvent event);

  public void onAppEnd(DeployAppEvent event);

  public void onLog(DeployAppLogEvent event);

  public void onError(DeployAppExceptionEvent event);

  public void onAppSuccess(DeployAppEvent event);
}
