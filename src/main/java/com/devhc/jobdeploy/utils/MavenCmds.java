package com.devhc.jobdeploy.utils;

public class MavenCmds {
  public static final String buildJarAndInstall = "mvn -q install";
  public static final String clean = "mvn clean";
  public static final String install = "mvn -q install";
  public static final String buidJar = "mvn package -DskipTests=true";
  public static final String buildJarAssembly = "mvn assembly:assembly -DskipTests=true";
  public static final String buildJarCopyDep = "mvn dependency:copy-dependencies -DoutputDirectory=lib package";
  public static final String eclipse = "mvn -q eclipse:eclipse -DdownloadSources";
  public static final String releaseToRepository =
    "mvn -q deploy:deploy-file -DgroupId={GROUP_ID} -DartifactId={ARTIFACT_ID}"
      + " -Dversion={VERSION} -Dpackaging=jar -Dfile=target/{TARGET_FILE}"
      + " -Durl={URL} -DrepositoryId={REPOSITORY_ID}";
  public static final String packageSingle = "mvn validate package assembly:single";
}
