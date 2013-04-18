package com.cloudbees.genapp;

import java.util.Map;
import java.util.TreeMap;

/**
 * The Metadata class stores Resources entries by name in a map.
 */

public class Metadata {
    private TreeMap<String, Resource> resources;
    
    public Metadata() {
        resources = new TreeMap<String, Resource>();
    }
    
    public Metadata(Metadata metadata) {
        resources = new TreeMap<String, Resource> (metadata.resources);
    }

    public Map<String, Resource> getResources() {
        return resources;
    }
    
    public Metadata addResource(Resource resource) {
        resources.put(resource.getName(), resource);
        return this;
    }
    
    public Metadata addResources(Map<String, Resource> addedResources) {
        resources.putAll(addedResources);
        return this;
    }
}