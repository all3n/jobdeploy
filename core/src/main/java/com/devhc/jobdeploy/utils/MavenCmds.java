package com.devhc.jobdeploy.utils;

public class MavenCmds {

  public static final String buildJarAndInstall = "-q install";
  public static final String clean = "clean";
  public static final String install = "-q install";
  public static final String buidJar = "package -DskipTests=true";
  public static final String buildJarAssembly = "assembly:assembly -DskipTests=true";
  public static final String buildJarCopyDep = "dependency:copy-dependencies -DoutputDirectory=lib package";
  public static final String eclipse = "-q eclipse:eclipse -DdownloadSources";
  public static final String releaseToRepository =
      "-q deploy:deploy-file -DgroupId={GROUP_ID} -DartifactId={ARTIFACT_ID}"
          + " -Dversion={VERSION} -Dpackaging=jar -Dfile=target/{TARGET_FILE}"
          + " -Durl={URL} -DrepositoryId={REPOSITORY_ID}";
  public static final String packageSingle = "validate package assembly:single";
}
