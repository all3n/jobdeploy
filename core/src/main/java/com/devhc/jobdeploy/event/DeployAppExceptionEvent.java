package com.devhc.jobdeploy.event;

import java.util.EventObject;

/**
 * Created by wanghch on 16/11/22.
 */
public class DeployAppExceptionEvent extends EventObject {

  private Exception exception;

  /**
   * Constructs a prototypical Event.
   *
   * @param source The object on which the Event initially occurred.
   * @throws IllegalArgumentException if source is null.
   */
  public DeployAppExceptionEvent(Object source, Exception e) {
    super(source);
    this.exception = e;
  }

  public Exception getException() {
    return exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }
}
