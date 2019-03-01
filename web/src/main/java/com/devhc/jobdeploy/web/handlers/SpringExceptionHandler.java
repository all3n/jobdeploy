package com.devhc.jobdeploy.web.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class SpringExceptionHandler {

  /**
   * 全局处理Exception 错误的情况下返回500
   */
  @ExceptionHandler(value = {Exception.class})
  public ModelAndView handleOtherExceptions(final Exception ex, final WebRequest req) {
    ModelAndView tResult = new ModelAndView("error/5xx");
    String exp = ExceptionUtils.getStackTrace(ex);
    tResult.addObject("msg", ex.getMessage());
    tResult.addObject("exception", exp);
    return tResult;
  }

}
