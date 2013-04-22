package com.cloudbees.genapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * The MetadataBuilder class builds a Metadata instance from the metadata.json File or InputStream.
 */

public class MetadataBuilder {
    private Metadata metadata;

    private MetadataBuilder() {
        metadata = new Metadata();
    }

    private MetadataBuilder(Metadata metadata) {
        this.metadata = metadata;
    }

    private MetadataBuilder(MetadataBuilder metadataBuilder) {
        metadata = metadataBuilder.metadata;
    }

    /**
     * @param metadataFile The metadata.json file
     * @return A new Metadata instance, containing all resources parsed
     * from the JSON metadata given as input.
     * @throws IOException
     */
    public static Metadata fromFile(File metadataFile) throws IOException {
        FileInputStream metadataInputStream = new FileInputStream(metadataFile);
        try {
            return fromStream(metadataInputStream);
        } finally {
            metadataInputStream.close();
        }
    }

    /**
     * @param metadataInputStream An InputStream to read the JSON metadata from.
     * @return A new Metadata instance, containing all resources parsed
     * from the JSON metadata given as input.
     * @throws IOException
     */
    public static Metadata fromStream(InputStream metadataInputStream) throws IOException {
        ObjectMapper metadataObjectMapper = new ObjectMapper();

        JsonNode metadataRootNode = metadataObjectMapper.readTree(metadataInputStream);
        MetadataBuilder metadataBuilder = new MetadataBuilder();

        metadataBuilder = metadataBuilder.buildResources(metadataRootNode);
        return metadataBuilder.metadata;
    }

    /**
    * Parses resources and returns them in a new MetadataBuilder instance.
    * @param metadataRootNode The root node of the Json metadata to be parsed.
    * @return A new MetadataBuilder instance containing all parsed resources.
    **/
    private MetadataBuilder buildResources(JsonNode metadataRootNode) {
        Map<String, Resource> resources = new TreeMap<String, Resource>();

        for (Iterator<Map.Entry<String, JsonNode>> resourceFields = metadataRootNode.fields();
            resourceFields.hasNext(); ) {
            Map.Entry<String, JsonNode> resourceEntry = resourceFields.next();
            JsonNode resourceContent = resourceEntry.getValue();

            // We check if the entry is a valid resource.
            if (resourceContent.has("__resource_type__") && resourceContent.has("__resource_name__")) {
                JsonNode resourceNameNode = resourceContent.get("__resource_name__");
                JsonNode resourceTypeNode = resourceContent.get("__resource_type__");

                // We check if the reserved attributes are well-formed.
                if(resourceNameNode.isTextual() && resourceTypeNode.isTextual()) {
                    String resourceName = resourceNameNode.asText();
                    String resourceType = resourceTypeNode.asText();
                    Resource resource = new Resource(resourceName, resourceType);

                    // We then collect the resource's properties
                    for (Iterator<Map.Entry<String, JsonNode>> resourceProperties = resourceContent.fields();
                         resourceProperties.hasNext(); ) {
                        Map.Entry<String, JsonNode> resourceProperty = resourceProperties.next();
                        String resourcePropertyName = resourceProperty.getKey();
                        JsonNode resourcePropertyValueNode = resourceProperty.getValue();

                        // We check if the current property not a reserved property ( ~ __.*__ ) and is well-formed.
                        if (!resourcePropertyName.matches("^__.*__$")
                                && resourcePropertyValueNode.isTextual()) {
                            String resourcePropertyValue = resourcePropertyValueNode.asText();
                            resource.addProperty(resourcePropertyName, resourcePropertyValue);
                        }
                    }
                    resources.put(resourceName, resource);
                }
            }
        }
        return new MetadataBuilder(metadata.addResources(resources));
    }
}