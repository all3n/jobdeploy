package com.devhc.jobdeploy.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface TaskOption {
  public String value();

  public String longOpt() default "";

  public boolean hasArgs() default false;

  public boolean required() default false;

  public String description() default "";
}
