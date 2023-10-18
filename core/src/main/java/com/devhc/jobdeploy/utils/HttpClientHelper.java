package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.exception.DeployException;
import com.google.common.collect.Maps;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

public class HttpClientHelper {

  private final Logger LOG = Loggers.get();

  private final RequestConfig config;
  private CloseableHttpClient client;
  private String auth;
  private HttpClientContext ctx;

  public HttpClientHelper() {
    this.config = RequestConfig.custom().setConnectTimeout(29999).setSocketTimeout(50000)
        .build();
  }

  public void setAuth(String auth) {
    this.auth = auth;
  }

  public void init() {
    try {
      if (StringUtils.isNotEmpty(auth)) {
        BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        String authInfo[] = auth.split(":");
        bcp.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(authInfo[0], authInfo[1]));

        this.ctx = HttpClientContext.create();
        BasicAuthCache authCache = new BasicAuthCache();
        ctx.setAuthCache(authCache);
        ctx.setCredentialsProvider(bcp);
        this.client = HttpClientBuilder.create()
            .setDefaultRequestConfig(config)
            .build();
      } else {
        this.client = HttpClientBuilder.create()
            .setDefaultRequestConfig(config)
            .build();
      }
    } catch (Exception e) {
      throw new DeployException(e);
    }
  }
  public Object get(String url, Map<String, String> params, Map<String, String> headers) {
    HttpGet get = new HttpGet(url);
    if(StringUtils.isNotEmpty(auth)) {
      try {
        URL u = new URL(url);
        HttpHost hh = new HttpHost(u.getHost(), u.getPort());
        ctx.getAuthCache().put(hh, new BasicScheme());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    for (String key : params.keySet()) {
      nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
    }
    try {
      if (headers != null) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
          get.setHeader(e.getKey(), e.getValue());
        }
      }
      CloseableHttpResponse res = client.execute(get, ctx);
      String result = EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8);
      LOG.info("{}", result);
      return result;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Object post(String url, Map<String, String> params, Map<String, String> headers) {
    HttpPost post = new HttpPost(url);
    if(StringUtils.isNotEmpty(auth)) {
      try {
        URL u = new URL(url);
        HttpHost hh = new HttpHost(u.getHost(), u.getPort());
        ctx.getAuthCache().put(hh, new BasicScheme());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    for (String key : params.keySet()) {
      nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
    }
    try {
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
      if (headers != null) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
          post.setHeader(e.getKey(), e.getValue());
        }
      }
      CloseableHttpResponse res = client.execute(post, ctx);
      String result = EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8);
      LOG.info("{}", result);
      return result;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void downloadFile(String fileUrl, String savePath) {

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet httpGet = new HttpGet(fileUrl);

      try (CloseableHttpResponse response = httpClient.execute(httpGet);
           InputStream inputStream = response.getEntity().getContent();
           FileOutputStream outputStream = new FileOutputStream(savePath)) {

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
