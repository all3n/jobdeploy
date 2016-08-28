package com.devhc.jobdeploy.manager;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class CompressManager {

  public void createTgz(String compressPath, String tarGzPath, String tgzDirName) throws IOException {
    FileOutputStream fOut = null;
    BufferedOutputStream bOut = null;
    GzipCompressorOutputStream gzOut = null;
    TarArchiveOutputStream tOut = null;
    try {
      fOut = new FileOutputStream(new File(tarGzPath));
      bOut = new BufferedOutputStream(fOut);
      gzOut = new GzipCompressorOutputStream(bOut);
      tOut = new TarArchiveOutputStream(gzOut);
      addFileToTarGz(tOut, compressPath, "", tgzDirName);
    } finally {
      tOut.finish();
      IOUtils.closeQuietly(tOut);
      IOUtils.closeQuietly(gzOut);
      IOUtils.closeQuietly(bOut);
      IOUtils.closeQuietly(fOut);
    }
  }

  private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base, String folderName)
    throws IOException {
    File f = new File(path);
    String entryName = base + f.getName();
    if (StringUtils.isNotEmpty(folderName)) {
      entryName = base + folderName;
    }
    TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
    tOut.putArchiveEntry(tarEntry);

    if (f.isFile()) {
      IOUtils.copy(new FileInputStream(f), tOut);
      tOut.closeArchiveEntry();
    } else {
      tOut.closeArchiveEntry();
      File[] children = f.listFiles();
      if (children != null) {
        for (File child : children) {
          addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/", "");
        }
      }
    }
  }
}
