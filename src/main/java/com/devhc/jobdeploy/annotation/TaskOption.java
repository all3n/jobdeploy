package com.devhc.jobdeploy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

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
