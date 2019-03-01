package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.BaseTest;
import com.devhc.jobdeploy.config.DeployJson;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by wanghch on 16/9/1.
 */
@Ignore
public class CleanTaskTest extends BaseTest {

  @Autowired
  DeployJson dc;
  @Autowired
  CleanTask task;

  public CleanTaskTest() {
  }

  @Before
  public void setUp() throws Exception {
    dc.put("servers", new JSONArray("[{server: '', deployto:'"));
    dc.put("keep_releases", 3);
  }

  @Test
  public void test() throws Exception {
    task.exec();
  }
}
