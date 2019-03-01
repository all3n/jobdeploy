package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.BaseTest;
import com.devhc.jobdeploy.config.DeployJson;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class NotifyTaskTest extends BaseTest {

  @Autowired
  DeployJson dc;
  @Autowired
  NotifyTask nt;

  public NotifyTaskTest() {
  }

  @Before
  public void setUp() throws Exception {
    dc.put("notify_email", new JSONArray("[]"));
    dc.put("servers", new JSONArray("[{server: 'unit test case', deployto:'/target/path'}]"));
  }

  @Test
  public void test() {
    nt.exec();
  }

}
