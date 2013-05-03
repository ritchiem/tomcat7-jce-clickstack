package com.cloudbees.genapp.metadata;

import java.io.File;
import java.util.Map;

public class MetadataFinder {

    /**
     * The main method takes optional arguments for the location of the
     * context.xml file to modify, as well as the location of the metadata.json
     * file. Defaults are:
     * CONTEXT_XML_PATH = $app_dir/server/conf/context.xml
     * METADATA_PATH = $genapp_dir/metadata.json
     * @throws Exception
     */

    public static void setup (String configurationRelativePath, ConfigurationBuilder configurationBuilder)
        throws Exception{
        setup(configurationRelativePath, configurationBuilder, null, null);
    }
    public static void setup (String configurationRelativePath, ConfigurationBuilder configurationBuilder,
                              String defaultMetadataPath, String defaultConfigurationPath)
            throws Exception {

        Map<String, String> env = System.getenv();
        String configurationPath;
        String metadataPath;

        if (defaultConfigurationPath != null)
            configurationPath = defaultConfigurationPath;
        else
            configurationPath = env.get("app_dir") + configurationRelativePath;

        if (defaultMetadataPath != null)
            metadataPath = defaultMetadataPath;
        else
            metadataPath = env.get("genapp_dir") + "/metadata.json";

        // Locate Tomcat 7 context file
        File configurationFile = new File(configurationPath);
        if (!configurationFile.exists())
            throw new Exception("Missing context config file: " + configurationFile.getAbsolutePath());

        // Locate genapp's metadata.json
        File metadataJson = new File(metadataPath);
        if (!metadataJson.exists())
            throw new Exception("Missing metadata file: " + metadataJson.getAbsolutePath());


        // Load the metadata and inject its settings into the server context Document
        Metadata metadata = Metadata.Builder.fromFile(metadataJson);
        configurationBuilder.writeConfiguration(metadata, configurationFile);
    }
}
