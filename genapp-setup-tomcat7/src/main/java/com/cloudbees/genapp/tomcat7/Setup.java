package com.cloudbees.genapp.tomcat7;

import com.cloudbees.genapp.metadata.*;

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
        MetadataFinder metadataFinder = new MetadataFinder();
        Metadata metadata = metadataFinder.getMetadata();

        EnvBuilder safeEnvBuilder = new EnvBuilder(true, false, metadata);
        safeEnvBuilder.writeControlFile("/env_safe");
        ContextXmlBuilder contextXmlBuilder = new ContextXmlBuilder(metadata);
        contextXmlBuilder.injectToFile("/server/conf/context.xml");
    }
}
