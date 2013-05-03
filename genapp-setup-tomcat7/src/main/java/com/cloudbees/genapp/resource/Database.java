package com.cloudbees.genapp.resource;

import java.util.Arrays;
import java.util.List;

public class Database extends Resource {

    public static final String URL_PROPERTY = "DATABASE_URL";
    public static final String USERNAME_PROPERTY = "DATABASE_USERNAME";
    public static final String PASSWORD_PROPERTY = "DATABASE_PASSWORD";
    public static final List<String> TYPES = Arrays.asList("datasource", "database");

    public static String getDriver(String url) {
        return "mysql";
    }

    public static String getJavaDriver(String driver) {
        return "com.mysql.jdbc.Driver";
    }

    static boolean checkResource(Resource resource) {
        boolean isValid;
        if (isValid = resource != null) {
            isValid = TYPES.contains(resource.getType());
            isValid = isValid && resource.getProperty(URL_PROPERTY) != null;
            isValid = isValid && resource.getProperty(USERNAME_PROPERTY) != null;
            isValid = isValid && resource.getProperty(PASSWORD_PROPERTY) != null;
        }
        return isValid;
    }

    Database (Resource resource) {
        super(resource.getProperties(), resource.getDescriptors());
        if (!checkResource(resource))
            throw new IllegalArgumentException("Incorrect database resource definition.");
    }

    public String getUrl() {
        return getProperty(URL_PROPERTY);
    }

    public String getUsername() {
        return getProperty(USERNAME_PROPERTY);
    }

    public String getPassword() {
        return getProperty(PASSWORD_PROPERTY);
    }

    public String getDriver() {
        return getDriver(getProperty(URL_PROPERTY));
    }

    public String getJavaDriver() {
        return getJavaDriver(getDriver(getProperty(URL_PROPERTY)));
    }
}