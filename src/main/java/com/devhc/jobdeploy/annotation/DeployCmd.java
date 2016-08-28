package com.devhc.jobdeploy.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DeployCmd {
  public String value();

  public String longOpt() default "";

  public boolean hasArgs() default false;

  public String description() default "";
}
