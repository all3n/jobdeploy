package com.devhc.jobdeploy.config.parser;

/**
 * Created by wanghch on 16/11/11.
 */
public interface JsonAbstractParser<IN_TYPE,OUT_TYPE> {
  public OUT_TYPE  parse(IN_TYPE a);
}
