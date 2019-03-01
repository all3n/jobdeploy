package com.devhc.jobdeploy.azkaban.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AzkabanArchiveGenerator {

  private static final int BUFFER = 8192;

  private String projectLocation;

  public AzkabanArchiveGenerator(Project project) {
    this.projectLocation = project.getLocation() + File.separator + project.getName();
  }

  public byte[] generateProjectPackages() throws IOException {
    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

    CheckedOutputStream cos = new CheckedOutputStream(byteOutput, new CRC32());
    ZipOutputStream out = new ZipOutputStream(cos);
    compress(new File(projectLocation), out, "");
    out.close();

    return byteOutput.toByteArray();
  }

  private void compress(File file, ZipOutputStream out, String basedir) {
    if (file.isDirectory()) {
      this.compressDirectory(file, out, basedir);
    } else {
      this.compressFile(file, out, basedir);
    }
  }

  private void compressDirectory(File dir, ZipOutputStream out, String basedir) {
    if (!dir.exists()) {
      return;
    }

    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      compress(files[i], out, basedir + dir.getName() + "/");
    }
  }

  private void compressFile(File file, ZipOutputStream out, String basedir) {
    if (!file.exists()) {
      return;
    }
    try {
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
      ZipEntry entry = new ZipEntry(basedir + file.getName());
      out.putNextEntry(entry);
      int count;
      byte data[] = new byte[BUFFER];
      while ((count = bis.read(data, 0, BUFFER)) != -1) {
        out.write(data, 0, count);
      }
      bis.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
