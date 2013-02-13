package com.cloudbees.genapp.tomcat7;

import java.io.File;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.cloudbees.genapp.GenappMetadata;
import com.cloudbees.genapp.MetadataBuilder;

public class Setup {
    public static void main(String[] args) throws Exception {
        String appDirPath = args[0];
        System.out.println("Launching: " + appDirPath);

        // locate root genapp app_dir
        File appDir = new File(appDirPath);
        if (!appDir.exists() || !appDir.isDirectory())
            throw new Exception("Invalid arg[0] - appdir is invalid: "
                    + appDir.getAbsolutePath());

        // locate metadata.json (relative to app_dir)
        File metadataJson = new File(appDirPath, ".genapp/metadata.json");
        if (!metadataJson.exists())
            throw new Exception("Missing metadata file: "
                    + metadataJson.getAbsolutePath());

        // locate Tomcat7 context.xml file (relative to app_dir)
        File serverConf = new File(appDir, "server/conf/context.xml");
        if (!serverConf.exists())
            throw new Exception("Missing context config file: "
                    + serverConf.getAbsolutePath());

        // Load the metadata and inject its settings into the context.xml
        GenappMetadata md = MetadataBuilder.fromFile(metadataJson);
        Document doc = ContextXmlBuilder.create(md).fromExistingDoc(serverConf)
                .buildDocument();

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        transformer.transform(new DOMSource(doc), new StreamResult(serverConf));
    }
}
