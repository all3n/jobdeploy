package com.devhc.jobdeploy.strategy;

import org.junit.Test;

import static org.junit.Assert.*;

public class StrategyTest {

  @Test
  public void test_parse_not_arg() {
    Strategy s = Strategy.parse("maven:assembly");
    assertEquals("maven:assembly", s.getName());
    assertNull(s.getArgs());
  }
  

  @Test
  public void test_parse_has_arg() {
    Strategy s = Strategy.parse("maven:assembly:archive[tgz]");
    assertEquals("maven:assembly:archive", s.getName());
    assertEquals("tgz", s.getArgs());
  }
  
  

}
