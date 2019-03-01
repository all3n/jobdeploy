package com.devhc.jobdeploy.azkaban.client;

import static org.junit.Assert.assertNotNull;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AzkabanClientTest {

  private static AzkabanClient client;
  private static String testProjectName;

  @BeforeClass
  public static void setup() throws Exception {
    Project project = new Project();
    project.setDescription("test jobdeploy");

    testProjectName = "jobdeploy-" + System.currentTimeMillis();
    project.setName(testProjectName);
    Account account = new Account();
    account.setPassword("");
    account.setUsername("");
    String serverLocation = "";

    client = new AzkabanClient(project, account, serverLocation);
    File tmpUploadJobBaseDir = Files.createTempDir();
    project.setLocation(tmpUploadJobBaseDir.getPath());
    String jobDir = tmpUploadJobBaseDir.getPath() + File.separatorChar + testProjectName;
    File tmpUploadJobDir = new File(jobDir);
    tmpUploadJobDir.mkdirs();

    InputStream fromIs = AzkabanClientTest.class.getClassLoader()
        .getResourceAsStream("jobs/test.job");
    InputStreamReader isr = new InputStreamReader(fromIs);
    BufferedReader br = new BufferedReader(isr);

    File to = new File(jobDir + File.separatorChar + "test.job");

    String line = null;
    FileOutputStream fos = new FileOutputStream(to);
    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    BufferedWriter bw = new BufferedWriter(osw);
    while ((line = br.readLine()) != null) {
      bw.write(line);
      bw.newLine();
    }
    IOUtils.closeQuietly(bw);
    IOUtils.closeQuietly(osw);
    IOUtils.closeQuietly(fos);

    IOUtils.closeQuietly(br);
    IOUtils.closeQuietly(isr);
    IOUtils.closeQuietly(fromIs);

    System.out.println(tmpUploadJobDir.getName() + ":" + tmpUploadJobDir.getPath());

  }

  @Test
  public void testAzkabanClient() {
  }

  @Test
  public void testCreateProject() throws ClientProtocolException, IOException {
    client.createProject();
  }

  @Test
  public void testUpload() throws JSONException, IOException {
    client.upload();
  }

  @Test
  public void testFetchFlows() throws ClientProtocolException, IOException {
    System.out.println(client.fetchProjectFlows());
  }

  @Test
  public void testGetSessionId() {
    assertNotNull(client.getSessionId());
  }

  @AfterClass
  public static void afterClass() throws ClientProtocolException, IOException {
    client.deleteProject();
  }

}
