package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.exception.DeployException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;

public class FileUtils {

    public static Logger log = Loggers.get();

    public static String getExecDir() {
        File local = new File(".");
        try {
            return local.getCanonicalFile().getPath();
        } catch (IOException e) {
            throw new DeployException(e);
        }
    }
    public static short translatePosixPermissionToMode(Set<PosixFilePermission> permission) {
        int mode = 0;
        for (PosixFilePermission action : PosixFilePermission.values()) {
            mode = mode << 1;
            mode += permission.contains(action) ? 1 : 0;
        }
        return (short) mode;
    }

    public static List<String> glob(String glob, String location) throws IOException {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
            glob);
        List<String> out = new ArrayList<String>();
        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path,
                BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    out.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return out;
    }

    public static void copyFileToDir(File jarFileObj, String realpath) throws IOException {
        File targetPath = new File(realpath);
        if (!targetPath.exists()) {
            log.info(realpath + " not exist,create");
            targetPath.mkdirs();
        }
        String targetFilePath = realpath + File.separator + jarFileObj.getName();
        File targetFile = new File(targetFilePath);
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(Files.newInputStream(jarFileObj.toPath()));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(Files.newOutputStream(targetFile.toPath()));

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
            if (inBuff != null) {
                inBuff.close();
            }
            if (outBuff != null) {
                outBuff.close();
            }
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
            if (inBuff != null) {
                inBuff.close();
            }
            if (outBuff != null) {
                outBuff.close();
            }
        }
    }

    public static String getDeployTmpDir(String tmpDir) {
        String deployTmp = System.getProperty("user.home") + File.separator + ".deploy" + File.separator + tmpDir;
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
                basePath.lastIndexOf(File.separator));
        }
        return jarPath;
    }


    public static String expandHome(String path) {
        if (path.startsWith("~")) {
            String home = System.getProperty("user.home");
            path = home + path.substring(1);
        }
        return path;
    }

}
