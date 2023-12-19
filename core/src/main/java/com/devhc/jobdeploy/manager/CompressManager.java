package com.devhc.jobdeploy.manager;

import com.devhc.jobdeploy.config.structs.DeployPattern;
import com.devhc.jobdeploy.utils.FileUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CompressManager {

  public void createTgz(String compressPath, String tarGzPath, String tgzDirName)
      throws IOException {
    createTgz(compressPath, tarGzPath, tgzDirName, null);
  }

  public void createTgz(String compressPath, String tarGzPath, String tgzDirName,
      DeployPattern dp)
      throws IOException {
    FileOutputStream fOut = null;
    BufferedOutputStream bOut = null;
    GzipCompressorOutputStream gzOut = null;
    TarArchiveOutputStream tOut = null;
    try {
      fOut = new FileOutputStream(new File(tarGzPath));
      bOut = new BufferedOutputStream(fOut);
      gzOut = new GzipCompressorOutputStream(bOut);
      tOut = new TarArchiveOutputStream(gzOut);
      tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      addFileToTarGz(tOut, compressPath, "", tgzDirName, dp);
    } finally {
      tOut.finish();
      IOUtils.closeQuietly(tOut);
      IOUtils.closeQuietly(gzOut);
      IOUtils.closeQuietly(bOut);
      IOUtils.closeQuietly(fOut);
    }
  }

  private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base,
      String folderName, DeployPattern dp)
      throws IOException {
    File f = new File(path);
    String entryName = base + f.getName();
    if (StringUtils.isNotEmpty(folderName)) {
      entryName = base + folderName;
    }
    if (dp.filter(entryName)) {
      return;
    }
    TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
    if (f.isFile()) {
      int mode = FileUtils.translatePosixPermissionToMode(
          Files.getPosixFilePermissions(f.toPath()));
      tarEntry.setMode(mode);
      tOut.putArchiveEntry(tarEntry);
      IOUtils.copy(Files.newInputStream(f.toPath()), tOut);
      tOut.closeArchiveEntry();
    } else {
      tOut.putArchiveEntry(tarEntry);
      tOut.closeArchiveEntry();
      File[] children = f.listFiles();
      if (children != null) {
        for (File child : children) {
          addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/", "", dp);
        }
      }
    }
  }
}
