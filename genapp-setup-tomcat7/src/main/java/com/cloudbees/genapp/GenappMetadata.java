package com.cloudbees.genapp;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GenappMetadata {
    public Map<String, String> appEnv;
    public Map<String, DataSource> datasources;
    
    public GenappMetadata() {
        this.appEnv = new TreeMap<String, String>();
        this.datasources = new TreeMap<String, DataSource>();
    }
    
    public GenappMetadata(GenappMetadata md) {
        this.appEnv = new TreeMap<String, String>(md.appEnv);
        this.datasources = new TreeMap<String, DataSource>(md.datasources);
    }
    
    public GenappMetadata addEnvironment(Map<String, String> env) {
        GenappMetadata md = new GenappMetadata(this);
        md.appEnv.putAll(env);
        return md;
    }
    
    public GenappMetadata addDatasource(DataSource ds) {
        GenappMetadata md = new GenappMetadata(this);
        md.datasources.put(ds.alias, ds);
        return md;
    }
    
    public GenappMetadata addDataSources(Map<String, DataSource> ds) {
        GenappMetadata md = new GenappMetadata(this);
        md.datasources.putAll(ds);
        return md;
    }

    public static class DataSource {
        public String alias;
        public Map<String, String> properties = new HashMap<String, String>();
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((alias == null) ? 0 : alias.hashCode());
            result = prime * result
                    + ((properties == null) ? 0 : properties.hashCode());
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
            DataSource other = (DataSource) obj;
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
            return true;
        }
    }
}