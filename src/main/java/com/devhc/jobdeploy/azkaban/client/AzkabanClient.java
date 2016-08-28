package com.devhc.jobdeploy.azkaban.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.devhc.jobdeploy.exception.DeployException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AzkabanClient {
  private static Logger log = LoggerFactory.getLogger(AzkabanClient.class);
  private String serverLocation;
  private Project project;
  private Account account;

  private HttpClient client;
  private String sessionId;

  public AzkabanClient(Project project, Account account, String serverLocation)
    throws Exception {
    this.serverLocation = serverLocation;
    this.project = project;
    this.account = account;
    initilizeClient();
  }

  public boolean createProject()
    throws ClientProtocolException, IOException {
    log.info("create project:{}", project.getName());
    Map<String, String> params = Maps.newHashMap();
    params.put("session.id", getSessionId());
    params.put("name", project.getName());
    params.put("description", project.getDescription());
    HttpPost req = postForm(serverLocation + File.separatorChar + "manager?action=create", params);
    HttpResponse response = client.execute(req);
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      return false;
    }
    JSONObject responseObject = new JSONObject(EntityUtils.toString(response.getEntity()));
    log.info("{}", responseObject);
    String status = responseObject.getString("status");

    if ("error".equals(status)) {
      String message = responseObject.getString("message");
      log.error("create project faild !!!! with reason \"" + message + "\"");
    }
    return true;
  }

  public boolean isProjectExist() throws IOException {
    return !fetchProjectFlows().isEmpty();
  }

  public List<String> fetchProjectFlows()
    throws IOException {
    List<String> flows = Lists.newArrayList();
    String url = serverLocation + File.separatorChar + "manager?ajax=fetchprojectflows&session.id=" + sessionId
      + "&project="
      + project.getName();
    HttpGet req = new HttpGet(url);
    HttpResponse response = client.execute(req);
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      return flows;
    }
    System.out.println(url);
    String repStr = EntityUtils.toString(response.getEntity());
    if (StringUtils.isNotEmpty(repStr)) {
      JSONObject responseObject = new JSONObject(repStr);
      JSONArray flowsJsonArr = responseObject.getJSONArray("flows");
      for (int i = 0; i < flowsJsonArr.length(); i++) {
        flows.add(((JSONObject) flowsJsonArr.get(i)).getString("flowId"));
      }
    }
    return flows;
  }

  /**
   * This method is used to initilize the client used to upload the project
   *
   * @throws NoSuchAlgorithmException
   * @throws KeyManagementException
   * @throws IOException
   * @throws JSONException
   */
  public void initilizeClient() throws NoSuchAlgorithmException, KeyManagementException, JSONException, IOException {
    if (serverLocation.contains("https")) {
      client = createSSLClientDefault();
    } else {
      RequestConfig config = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(50000).build();
      try {
        client = HttpClientBuilder.create().setDefaultRequestConfig(config)
          .build();
      } catch (Exception e) {
        throw new DeployException(e);
      }
    }

    initSessionId();
  }

  public static CloseableHttpClient createSSLClientDefault() {
    try {
      SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
        //信任所有
        public boolean isTrusted(X509Certificate[] chain,
          String authType) throws CertificateException {
          return true;
        }
      }).build();
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      return HttpClients.custom()
        .setSSLSocketFactory(sslsf).build();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    }
    return HttpClients.createDefault();
  }

  /**
   * This method is used to upload the project
   *
   * @return true if success or it will return false
   * @throws IOException
   * @throws JSONException
   */
  public boolean upload() throws IOException, JSONException {
    if (!isProjectExist()) {
      createProject();
    }
    HttpPost filePost = new HttpPost(serverLocation + File.separatorChar + "manager?ajax=upload");

    AzkabanArchiveGenerator generator = new AzkabanArchiveGenerator(project);
    byte[] jobContent = generator.generateProjectPackages();
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.addBinaryBody("file", jobContent, ContentType.create("application/zip"), project.getName() + ".zip");
    builder.addTextBody("ajax", "upload");
    builder.addTextBody("project", project.getName());
    builder.addTextBody("session.id", sessionId);
    HttpEntity reqEntity = builder.build();
    filePost.setEntity(reqEntity);

    try {
      HttpResponse response = client.execute(filePost);
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        return false;
      }

      JSONObject responseObject = new JSONObject((EntityUtils.toString(response.getEntity())));
      log.info("{}", responseObject);
      if (responseObject.has("error")) {
        System.out.println("Job deploy faild !!!! with reason \"" + responseObject.get("error") + "\"");
        return false;
      }

      return true;
    } finally {
      filePost.releaseConnection();
    }
  }

  /**
   * Log in process, used to get the login session id, or it will return null
   *
   * @return if log in fail it will return null.
   * @throws JSONException
   * @throws IOException
   */
  private String initSessionId() throws JSONException, IOException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("action", "login");
    params.put("username", account.getUsername());
    params.put("password", account.getPassword());
    HttpPost postMethod = postForm(serverLocation, params);
    this.sessionId = null;
    try {
      HttpResponse response = client.execute(postMethod);
      if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        System.out.println("Cannot login to the server [" + serverLocation + "]");
      }

      String responseStr = EntityUtils.toString(response.getEntity());
      log.info("{}", responseStr);
      JSONObject object = new JSONObject(responseStr);
      if (object.has("error")) {
        log.error("{}", object.get("error"));
        throw new DeployException(object.get("error").toString());
      }
      sessionId = object.getString("session.id");
    } finally {
      postMethod.releaseConnection();
    }
    return sessionId;
  }

  /**
   * This method is used to build up the post form.
   *
   * @param url
   * @param params
   * @return
   * @throws UnsupportedEncodingException
   */
  private HttpPost postForm(String url, Map<String, String> params) throws UnsupportedEncodingException {
    HttpPost httpost = new HttpPost(url);
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

    for (String key : params.keySet()) {
      nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
    }

    httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
    return httpost;
  }

  /**
   * The entry used of this client These properties can be passed into this
   * client -u <the username>, -p <the password>, -n <the project name>, -t <the
   * project path>, -l <azkaban url>
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args)
    throws Exception {
    Options options = new Options();
    options.addOption("u", "username", true, "the username of azkaban");
    options.addOption("p", "password", true, "the password of azkaban");
    options.addOption("n", "name", true, "the project name");
    options.addOption("t", "path", true, "the project path");
    options.addOption("l", "url", true, "the url of azkaban");
    options.addOption("d", "description", false, "desciption");

    CommandLineParser parser = new PosixParser();
    CommandLine commandLine = parser.parse(options, args);

    Account account = new Account();
    account.setUsername(commandLine.getOptionValue("username"));
    account.setPassword(commandLine.getOptionValue("password"));

    Project project = new Project();
    project.setName(commandLine.getOptionValue("name"));
    project.setLocation(commandLine.getOptionValue("path"));

    String serverLocation = commandLine.getOptionValue("url");

    String description = commandLine.getOptionValue("description");
    project.setDescription(description);

    boolean result = new AzkabanClient(project, account, serverLocation).upload();
    if (result) {
      System.out.println("Uploading Success!");
    } else {
      System.out.println("Uploading Fail!");
    }
    System.exit(result ? 0 : 1);
  }

  public boolean deleteProject() throws ClientProtocolException, IOException {
    String url = serverLocation + File.separatorChar + "manager?delete=true&session.id=" + sessionId + "&project="
      + project.getName();
    HttpGet req = new HttpGet(url);
    HttpResponse response = client.execute(req);
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      return false;
    }
    return true;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

}
