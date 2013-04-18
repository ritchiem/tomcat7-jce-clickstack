package com.cloudbees.genapp;

import java.util.Map;
import java.util.TreeMap;

/**
 * The GenappMetadata class stores GenappResources entries in a map by name.
 */

public class GenappMetadata {
    private TreeMap<String, GenappResource> genappResources;
    
    public GenappMetadata() {
        genappResources = new TreeMap<String, GenappResource>();
    }
    
    public GenappMetadata(GenappMetadata genappMetadata) {
        genappResources = new TreeMap<String, GenappResource> (genappMetadata.genappResources);
    }

    public Map<String, GenappResource> getResources() {
        return genappResources;
    }
    
    public GenappMetadata addResource(GenappResource genappResource) {
        genappResources.put(genappResource.getName(), genappResource);
        return this;
    }
    
    public GenappMetadata addResources(Map<String, GenappResource> addedGenappResources) {
        genappResources.putAll(addedGenappResources);
        return this;
    }
}