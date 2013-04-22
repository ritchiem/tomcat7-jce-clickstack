package com.cloudbees.genapp.tomcat7;

import java.io.File;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.cloudbees.genapp.Metadata;
import com.cloudbees.genapp.MetadataBuilder;

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

        // Locate Tomcat 7 context file
        File contextXml = new File(configPath);
        if (!contextXml.exists())
            throw new Exception("Missing context config file: " + contextXml.getAbsolutePath());

        // Locate genapp's metadata.json
        File metadataJson = new File(metadataPath);
        if (!metadataJson.exists())
            throw new Exception("Missing metadata file: " + metadataJson.getAbsolutePath());


        // Load the metadata and inject its settings into the server context Document
        Metadata metadata = MetadataBuilder.fromFile(metadataJson);
        Document contextDocument =
                ContextXmlBuilder.create(metadata).fromExistingDoc(contextXml).buildContextDocument();

        // Write the content into XML file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        transformer.transform(new DOMSource(contextDocument), new StreamResult(contextXml));
    }
}
