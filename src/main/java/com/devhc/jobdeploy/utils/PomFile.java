package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.exception.DeployException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class PomFile extends JSONObject {

  private PomFile() {
  }

  public String getGroupId() {
    return this.getString("groupId");
  }

  public String getArtifactId() {
    return this.getString("artifactId");
  }

  public String getName() {
    return this.getString("name");
  }

  public String getVersion() {
    return this.getString("version");
  }

  public static PomFile parsePomFile(String pomFilePath) {
    File pomFile = new File(pomFilePath);
    if (!pomFile.exists()) {
      throw new DeployException(pomFile + ":pom not exits");
    }
    PomFile pom = new PomFile();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    try {
      db = dbf.newDocumentBuilder();
      Document document = db.parse(pomFile);
      NodeList nl = document.getElementsByTagName("project").item(0)
        .getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        if (n.getNodeType() == Node.ELEMENT_NODE) {
          if (n.getChildNodes().getLength() == 1) {
            Node c = n.getFirstChild();
            String nodeValue = c.getNodeValue();
            pom.put(n.getNodeName(), nodeValue);
          }
        }
      }
    } catch (Exception e) {
      throw new DeployException(e);
    }
    return pom;
  }
}
