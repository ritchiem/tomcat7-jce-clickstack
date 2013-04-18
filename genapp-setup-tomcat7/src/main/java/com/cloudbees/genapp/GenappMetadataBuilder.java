package com.cloudbees.genapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * The MetadataBuilder class builds a GenappMetadata instance from the metadata.json File or InputStream.
 */

public class GenappMetadataBuilder {
    private GenappMetadata genappMetadata;

    private GenappMetadataBuilder() {
        genappMetadata = new GenappMetadata();
    }

    private GenappMetadataBuilder(GenappMetadata genappMetadata) {
        this.genappMetadata = genappMetadata;
    }

    private GenappMetadataBuilder(GenappMetadataBuilder metadataBuilder) {
        genappMetadata = metadataBuilder.genappMetadata;
    }

    /**
     * @param genappMetadataFile The metadata.json file
     * @return A new GenappMetadata instance, containing all resources parsed
     * from the JSON metadata given as input.
     * @throws Exception
     */
    public static GenappMetadata fromFile(File genappMetadataFile) throws IOException {
        FileInputStream genappMetadataInputStream = new FileInputStream(genappMetadataFile);
        try {
            return fromStream(genappMetadataInputStream);
        } finally {
            if (genappMetadataInputStream != null)
                genappMetadataInputStream.close();
        }
    }

    /**
     * @param genappMetadataInputStream An InputStream to read the JSON metadata from.
     * @return A new GenappMetadata instance, containing all resources parsed
     * from the JSON metadata given as input.
     * @throws Exception
     */
    public static GenappMetadata fromStream(InputStream genappMetadataInputStream) throws IOException {
        ObjectMapper genappMetadataObjectMapper = new ObjectMapper();

        JsonNode genappMetadataRootNode = genappMetadataObjectMapper.readTree(genappMetadataInputStream);
        GenappMetadataBuilder genappMetadataBuilder = new GenappMetadataBuilder();

        genappMetadataBuilder = genappMetadataBuilder.buildResources(genappMetadataRootNode);
        return genappMetadataBuilder.genappMetadata;
    }

    /**
    * Parses resources and returns them in a new MetadataBuilder instance.
    * @param genappMetadataRootNode The root node of the Json metadata to be parsed.
    * @return A new MetadataBuilder instance containing all parsed resources.
    **/
    private GenappMetadataBuilder buildResources(JsonNode genappMetadataRootNode) {
        Map<String, GenappResource> genappResources = new TreeMap<String, GenappResource>();

        for (Iterator<Map.Entry<String, JsonNode>> genappResourceFields = genappMetadataRootNode.fields();
            genappResourceFields.hasNext(); ) {
            Map.Entry<String, JsonNode> genappResourceEntry = genappResourceFields.next();
            JsonNode genappResourceContent = genappResourceEntry.getValue();

            // We check if the entry is a valid resource.
            if (genappResourceContent.has("__resource_type__") && genappResourceContent.has("__resource_name__")) {
                JsonNode genappResourceNameNode = genappResourceContent.get("__resource_name__");
                JsonNode genappResourceTypeNode = genappResourceContent.get("__resource_type__");

                // We check if the reserved attributes are well-formed.
                if(genappResourceNameNode.isTextual() && genappResourceTypeNode.isTextual()) {
                    String genappResourceName = genappResourceNameNode.asText();
                    String genappResourceType = genappResourceTypeNode.asText();
                    GenappResource genappResource = new GenappResource(genappResourceName, genappResourceType);

                    // We then collect the resource's properties
                    for (Iterator<Map.Entry<String, JsonNode>> genappResourceProperties = genappResourceContent.fields();
                         genappResourceProperties.hasNext(); ) {
                        Map.Entry<String, JsonNode> genappResourceProperty = genappResourceProperties.next();
                        String genappResourcePropertyName = genappResourceProperty.getKey();
                        JsonNode genappResourcePropertyValue = genappResourceProperty.getValue();

                        // We check if the current property not a reserved property ( ~ __.*__ ) and is well-formed.
                        if (!genappResourcePropertyName.matches("^__.*__$")
                                && genappResourcePropertyValue.isTextual()) {
                            String propertyValue = genappResourceProperty.getValue().asText();
                            genappResource.addProperty(genappResourcePropertyName, propertyValue);
                        }
                    }
                    genappResources.put(genappResourceName, genappResource);
                }
            }
        }
        return new GenappMetadataBuilder(genappMetadata.addResources(genappResources));
    }
}