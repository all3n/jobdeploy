package com.devhc.jobdeploy.manager;

import com.google.common.collect.Maps;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.utils.DeployUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.ServiceLoader;

@Lazy
@Component
public class StrategyManager {
  Map<String, ITaskStrategy> strategyMap = Maps.newHashMap();

  @PostConstruct
  public void init() {
    ServiceLoader<ITaskStrategy> loader = ServiceLoader.load(ITaskStrategy.class);
    for (ITaskStrategy is : loader) {
      String simpleClazzName = is.getClass().getSimpleName();
      String colonStr = DeployUtils.formatColonStr(simpleClazzName);
      strategyMap.put(colonStr, is);
    }
  }

  /**
   * 获取task strategy 实例
   * strategy class 命名规范 strategy+task
   * 同时需要在 META-INFO 下 service 进行注册
   * @param task
   * @param strategy
   * @return
   */
  public ITaskStrategy get(String task, String strategy) {
    return strategyMap.get((strategy + ":" + task).toLowerCase());
  }
}
