package com.cloudbees.genapp;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GenappMetadata {
    public Map<String, String> appEnv;
    public Map<String, Resource> resources;
    
    public GenappMetadata() {
        this.appEnv = new TreeMap<String, String>();
        this.resources = new TreeMap<String, Resource>();
    }
    
    public GenappMetadata(GenappMetadata md) {
        this.appEnv = new TreeMap<String, String>(md.appEnv);
        this.resources = new TreeMap<String, Resource>(md.resources);
    }
    
    public GenappMetadata addEnvironment(Map<String, String> env) {
        GenappMetadata md = new GenappMetadata(this);
        md.appEnv.putAll(env);
        return md;
    }
    
    public GenappMetadata addResource(Resource ds) {
        GenappMetadata md = new GenappMetadata(this);
        md.resources.put(ds.alias, ds);
        return md;
    }
    
    public GenappMetadata addResources(Map<String, Resource> ds) {
        GenappMetadata md = new GenappMetadata(this);
        md.resources.putAll(ds);
        return md;
    }
    
    public static class Resource {
        public static final String TYPE_DATABASE = "database";
        public static final String TYPE_MAIL = "mail";
        
        public String alias;
        public String type;
        public Map<String, String> properties = new HashMap<String, String>();
        
        public Resource(String alias, String type) {
            this.alias = alias;
            this.type = type;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((alias == null) ? 0 : alias.hashCode());
            result = prime * result
                    + ((properties == null) ? 0 : properties.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Resource other = (Resource) obj;
            if (alias == null) {
                if (other.alias != null)
                    return false;
            } else if (!alias.equals(other.alias))
                return false;
            if (properties == null) {
                if (other.properties != null)
                    return false;
            } else if (!properties.equals(other.properties))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
    }
}