package com.cloudbees.genapp;

import java.util.HashMap;
import java.util.Map;

/**
 * The Resource class stores a Genapp resource characterized by name, type and parameters.
 */

public class GenappResource {
    private String name;
    private String type;
    private HashMap<String, String> properties = new HashMap<String, String>();

    public GenappResource(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getProperty(String property) {
        return properties.get(property);
    }

    public String getProperty(String property, String defaultVal) {
        if (properties.containsKey(property))
            return properties.get(property);
        else
            return defaultVal;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public GenappResource addProperty(String parameter, String value) {
        properties.put(parameter, value);
        return this;
    }

    public GenappResource addProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
        return this;
    }

    /**
     * This method compounds the hashes for all of the object's variables, creating a hash for the Resource.
     * @return A simple, non-cryptographic hash for the Resource.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // The hash for a variable set to null is 0, and we skip the .hashCode() method call.
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object comparedObject) {

        // We check if this instance is the same as the compared Object and return true if it is.
        if (this == comparedObject)
            return true;

        // Then, if that failed, we make sure we are dealing with a GenappResource and return false if it's not.
        if (comparedObject == null)
            return false;
        if (getClass() != comparedObject.getClass())
            return false;

        // If it's a GenappResource, we check for the class' variables to match and return false if they don't.
        GenappResource comparedResource = (GenappResource) comparedObject;
        if (name == null) {
            if (comparedResource.name != null)
                return false;
        } else if (!name.equals(comparedResource.name)) {
            return false;
        }

        if (properties == null) {
            if (comparedResource.properties != null)
                return false;
        } else if (!properties.equals(comparedResource.properties)) {
            return false;
        }

        if (type == null) {
            if (comparedResource.type != null)
                return false;
        } else if (!type.equals(comparedResource.type)) {
            return false;
        }

        // If no difference is found then both GenappResources are equal and we return true.
        return true;
    }
}