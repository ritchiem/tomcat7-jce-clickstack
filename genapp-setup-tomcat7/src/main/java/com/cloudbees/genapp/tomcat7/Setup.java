package com.cloudbees.genapp.tomcat7;

import com.cloudbees.genapp.metadata.EnvBuilder;
import com.cloudbees.genapp.metadata.MetadataFinder;

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
        // Build the environment with bash-safe names, and no deprecated values.
        metadataFinder.setup("/.genapp/control/env_safe", new EnvBuilder(true, false));
        // Build the environment properties (bash-unsafe)
        metadataFinder.setup("/.genapp/control/env", new EnvBuilder(false, false));
        // Build Tomcat 7 context.xml file
        metadataFinder.setup("/server/conf/context.xml", new ContextXmlBuilder());
    }
}
