package com.devhc.jobdeploy.azkaban.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;

import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class AzkabanArchiveGenerator {
  private String projectLocation;

  public AzkabanArchiveGenerator(Project project) {
    this.projectLocation = project.getLocation() + File.separator + project.getName();
  }

  public byte[] generateProjectPackages() throws IOException {
    try (final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream()) {
      try (final ZipArchiveOutputStream out = new ZipArchiveOutputStream(byteOutput)) {
        compress(new File(projectLocation), out);
      }
      return byteOutput.toByteArray();
    }
  }

  private void compress(File file, ZipArchiveOutputStream out) throws IOException {
    if (file.exists()) {
      compress(file, out, file.getName());
    }
  }

  private void compress(File file, ZipArchiveOutputStream out, String zipEntryName) throws IOException {
    final ZipArchiveEntry zipEntry = new ZipArchiveEntry(file, zipEntryName);

    int mode = 0;
    for (final PosixFilePermission p : Files.readAttributes(file.toPath(), PosixFileAttributes.class).permissions()) {
      switch (p) {
        case OWNER_READ:     mode |= 0400; break;
        case OWNER_WRITE:    mode |= 0200; break;
        case OWNER_EXECUTE:  mode |= 0100; break;

        case GROUP_READ:     mode |= 0040; break;
        case GROUP_WRITE:    mode |= 0020; break;
        case GROUP_EXECUTE:  mode |= 0010; break;

        case OTHERS_READ:    mode |= 0004; break;
        case OTHERS_WRITE:   mode |= 0002; break;
        case OTHERS_EXECUTE: mode |= 0001; break;
      }
    }

    zipEntry.setUnixMode(mode | (file.isDirectory() ? UnixStat.DIR_FLAG : UnixStat.FILE_FLAG));
    out.putArchiveEntry(zipEntry);

    if (file.isDirectory()) {
      out.closeArchiveEntry();

      for (final File childFile : file.listFiles()) {
        // seperator is always '/'
        compress(childFile, out, zipEntryName + "/" + childFile.getName());
      }
    } else {
      try (final FileInputStream fis = new FileInputStream(file)) {
        IOUtils.copy(fis, out);
      }
      out.closeArchiveEntry();
    }
  }
}
