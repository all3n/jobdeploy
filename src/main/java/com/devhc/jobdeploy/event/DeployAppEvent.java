package com.devhc.jobdeploy.event;


import java.util.EventObject;

/**
 * Created by wanghch on 16/11/17.
 */
public class DeployAppEvent extends EventObject{
  /**
   * Constructs a prototypical Event.
   *
   * @param    source    The object on which the Event initially occurred.
   * @exception IllegalArgumentException  if source is null.
   */
  public DeployAppEvent(Object source) {
    super(source);
  }

}
