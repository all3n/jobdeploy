package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.exception.DeployException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;

public class FileUtils {
  public static Logger log = LoggerFactory.getLogger(FileUtils.class);

  public static String getExecDir() {
    File local = new File(".");
    try {
      return local.getCanonicalFile().getPath();
    } catch (IOException e) {
      throw new DeployException(e);
    }
  }

  public static void copyFileToDir(File jarFileObj, String realpath) throws IOException {
    File targetPath = new File(realpath);
    if (!targetPath.exists()) {
      log.info(realpath + " not exist,create");
      targetPath.mkdirs();
    }
    String targetFilePath = realpath + "/" + jarFileObj.getName();
    File targetFile = new File(targetFilePath);
    BufferedInputStream inBuff = null;
    BufferedOutputStream outBuff = null;
    try {
      // 新建文件输入流并对它进行缓冲
      inBuff = new BufferedInputStream(new FileInputStream(jarFileObj));

      // 新建文件输出流并对它进行缓冲
      outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

      // 缓冲数组
      byte[] b = new byte[1024 * 5];
      int len;
      while ((len = inBuff.read(b)) != -1) {
        outBuff.write(b, 0, len);
      }
      // 刷新此缓冲的输出流
      outBuff.flush();
    } finally {
      // 关闭流
      if (inBuff != null)
        inBuff.close();
      if (outBuff != null)
        outBuff.close();
    }
  }

  public static void copyFileToFile(File from, File to) throws IOException {
    BufferedInputStream inBuff = null;
    BufferedOutputStream outBuff = null;
    try {
      // 新建文件输入流并对它进行缓冲
      inBuff = new BufferedInputStream(new FileInputStream(from));

      // 新建文件输出流并对它进行缓冲
      outBuff = new BufferedOutputStream(new FileOutputStream(to));

      // 缓冲数组
      byte[] b = new byte[1024 * 5];
      int len;
      while ((len = inBuff.read(b)) != -1) {
        outBuff.write(b, 0, len);
      }
      // 刷新此缓冲的输出流
      outBuff.flush();
    } finally {
      // 关闭流
      if (inBuff != null)
        inBuff.close();
      if (outBuff != null)
        outBuff.close();
    }
  }

  public static String getDeployTmpDir(String tmpDir) {
    String deployTmp = System.getProperty("user.home") + "/.deploy/" + tmpDir;
    File tmpDirFile = new File(deployTmp);
    if (!tmpDirFile.exists()) {
      tmpDirFile.mkdirs();
    }
    return deployTmp;
  }

  public static String getJarDir() {
    String basePath = FileUtils.class.getProtectionDomain().getCodeSource()
      .getLocation().getPath();
    try {
      basePath = URLDecoder.decode(basePath, "utf-8");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    }
    String jarPath = null;
    if (basePath.endsWith(".jar")) {
      jarPath = basePath.substring(0,
        basePath.lastIndexOf("/"));
    }
    return jarPath;
  }

}
