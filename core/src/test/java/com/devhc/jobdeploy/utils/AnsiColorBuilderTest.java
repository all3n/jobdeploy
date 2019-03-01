package com.devhc.jobdeploy.utils;

import org.junit.Before;
import org.junit.Test;

public class AnsiColorBuilderTest {

  public AnsiColorBuilderTest() {
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void test() {
    System.out.println(AnsiColorBuilder.getRandomColor());
  }

}
