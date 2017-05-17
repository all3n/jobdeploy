package com.devhc.jobdeploy.event;

import com.devhc.jobdeploy.JobTask;

import java.util.Vector;

/**
 * Created by wanghch on 16/11/17.
 */
public class DeployAppLifeCycle {
  private Vector<DeployAppEventListener> listeners;

  public DeployAppLifeCycle() {
    listeners = new Vector<>();
  }

  public void addAppEventListener(DeployAppEventListener listener) {
    listeners.add(listener);
  }

  public void removeAppEventListener(DeployAppEventListener listener) {
    if (listener != null) {
      return;
    }
    listeners.remove(listener);
  }

  public void appStart() {
    DeployAppEvent data = new DeployAppEvent(this);
    listeners.forEach(listener -> listener.onAppStart(data));
  }

  public void appEnd() {
    DeployAppEvent data = new DeployAppEvent(this);
    listeners.forEach(listener -> listener.onAppEnd(data));
  }

  public void taskStart(JobTask task) {
    DeployAppTaskEvent data = new DeployAppTaskEvent(this, task);
    listeners.forEach(listener -> listener.onTaskStart(data));
  }

  public void taskEnd(JobTask task) {
    DeployAppTaskEvent data = new DeployAppTaskEvent(this, task);
    listeners.forEach(listener -> listener.onTaskEnd(data));
  }

  public void log(String msg, String level,String className) {
    DeployAppLogEvent logEvent = new DeployAppLogEvent(this, msg, level,className);
    listeners.forEach(listener -> listener.onLog(logEvent));
  }

  public void exceptionOccur(Exception e){
    DeployAppExceptionEvent data = new DeployAppExceptionEvent(this,e);
    listeners.forEach(listener -> listener.onError(data));
  }

  public void appSuccess(){
    DeployAppEvent data = new DeployAppEvent(this);
    listeners.forEach(listener -> listener.onAppSuccess(data));
  }
}
