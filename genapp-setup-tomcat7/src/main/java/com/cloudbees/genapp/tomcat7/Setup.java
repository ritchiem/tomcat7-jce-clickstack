package com.cloudbees.genapp.tomcat7;

import java.io.File;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.cloudbees.genapp.GenappMetadata;
import com.cloudbees.genapp.GenappMetadataBuilder;

/*
 * This class contains the main method to get the Genapp metadata and configure Tomcat 7.
 */

public class Setup {
    /**
     * The main method takes optional arguments for the location of the
     * context.xml file to modify, as well as the location of the metadata.json
     * file. Defaults are:
     * CONTEXT_XML_PATH = $app_dir/server/conf/context.xml
     * METADATA_PATH = $genapp_dir/metadata.json
     * @param args Two optional args: [ CONTEXT_XML_PATH [ METADATA_PATH ]]
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> env = System.getenv();
        String configPath;
        String metadataPath;

        if (args.length > 0)
            configPath = args[0];
        else
            configPath = env.get("app_dir") + "/server/conf/context.xml";

        if (args.length > 1)
            metadataPath = args[1];
        else
            metadataPath = env.get("genapp_dir") + "/metadata.json";

        // Locate Tomcat 7 configuration file
        File tomcatServerConfiguration = new File(configPath);
        if (!tomcatServerConfiguration.exists())
            throw new Exception("Missing context config file: " + tomcatServerConfiguration.getAbsolutePath());

        // Locate genapp's metadata.json
        File genappMetadataJson = new File(metadataPath);
        if (!genappMetadataJson.exists())
            throw new Exception("Missing metadata file: " + genappMetadataJson.getAbsolutePath());


        // Load the metadata and inject its settings into the server configuration Document
        GenappMetadata genappMetadata = GenappMetadataBuilder.fromFile(genappMetadataJson);
        Document configurationDocument = TomcatContextXmlBuilder
                .create(genappMetadata).fromExistingDoc(tomcatServerConfiguration).buildConfigurationDocument();

        // Write the content into XML file
        TransformerFactory xmlTransformerFactory = TransformerFactory.newInstance();
        Transformer xmlTransformer = xmlTransformerFactory.newTransformer();
        xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        xmlTransformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        xmlTransformer.transform(new DOMSource(configurationDocument), new StreamResult(tomcatServerConfiguration));
    }
}
