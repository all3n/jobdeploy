package com.devhc.jobdeploy.manager;

import com.devhc.jobdeploy.config.structs.DeployPattern;
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.utils.Loggers;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CompressManager {

  private static Logger LOG = Loggers.get();

  public void createTgz(String compressPath, String tarGzPath, String tgzDirName)
      throws IOException {
    createTgz(compressPath, tarGzPath, tgzDirName, null);
  }

  public void createTgz(String compressPath, String tarGzPath, String tgzDirName,
      DeployPattern dp)
      throws IOException {
    LOG.info("Creating tgz archive tgzDirName:{} compressPath:{} tarGzPath:{}", tgzDirName,
        compressPath, tarGzPath);
    try (
        FileOutputStream fOut = new FileOutputStream(tarGzPath);
        BufferedOutputStream bOut = new BufferedOutputStream(fOut);
        GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bOut);
        TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut);
    ) {
      tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      tOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
      addFileToTarGz(tOut, compressPath, "", tgzDirName, dp);
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
    if (dp != null && dp.filter(entryName)) {
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
