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
    private Metadata metadata;

    public MetadataFinder() throws Exception {
        this(null);
    }

    public MetadataFinder(String defaultMetadataPath) throws Exception {
        Map<String, String> env = System.getenv();
        String metadataPath;

        if (defaultMetadataPath != null)
            metadataPath = defaultMetadataPath;
        else
            metadataPath = env.get("genapp_dir") + "/metadata.json";

        // Locate genapp's metadata.json
        File metadataJson = new File(metadataPath);
        if (!metadataJson.exists())
            throw new Exception("Missing metadata file: " + metadataJson.getAbsolutePath());

        metadata = Metadata.Builder.fromFile(metadataJson);
    }

    public void setup (String configurationRelativePath, ConfigurationBuilder configurationBuilder)
        throws Exception{
        setup(configurationRelativePath, configurationBuilder, null);
    }

    public void setup (String configurationRelativePath, ConfigurationBuilder configurationBuilder,
                       String defaultConfigurationPath) throws Exception {

        Map<String, String> env = System.getenv();
        String configurationPath;

        if (defaultConfigurationPath != null)
            configurationPath = defaultConfigurationPath;
        else
            configurationPath = env.get("app_dir") + configurationRelativePath;

        // Locate configuration file
        File configurationFile = new File(configurationPath);
        if (!configurationFile.exists())
            throw new Exception("Missing context config file: " + configurationFile.getAbsolutePath());

        configurationBuilder.writeConfiguration(metadata, configurationFile);
    }
}
