package com.cloudbees.genapp.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA. User: benjamin Date: 5/6/13 Time: 1:21 PM To change this template use File | Settings |
 * File Templates.
 */
public class EnvBuilder implements ConfigurationBuilder {

    private boolean safe;
    private boolean deprecated;
    private Metadata metadata;
    private static final List<String> deprecatedKeys = Arrays.asList("^MYSQL_.*$");

    public EnvBuilder(boolean safe, boolean deprecated) {
        this.safe = safe;
        this.deprecated = deprecated;
    }

    private EnvBuilder(boolean safe, boolean deprecated, Metadata metadata) {
        this.safe = safe;
        this.deprecated = deprecated;
        this.metadata = metadata;
    }

    @Override
    public EnvBuilder create(Metadata metadata) {
        return new EnvBuilder(safe, deprecated, metadata);
    }

    private List<String> getProperties() {
        Vector<String> properties = new Vector<String>();
        for (Iterator<Map.Entry<String, String>> fields = metadata.getEnvironment().entrySet().iterator();
             fields.hasNext(); ) {
            Map.Entry<String, String> field = fields.next();
            String fieldKey = field.getKey();
            String fieldValue = field.getValue();
            if (!deprecated) {
                boolean isDeprecated = false;
                for (String deprecatedKey : deprecatedKeys) {
                    if (fieldKey.matches(deprecatedKey)) {
                        isDeprecated = true;
                    }
                }
                if (!isDeprecated) {
                    properties.add(formatProperty(fieldKey, fieldValue));
                }
            } else {
                properties.add(formatProperty(fieldKey, fieldValue));
            }
        }
        return properties;
    }

    private String formatProperty(String fieldKey, String fieldValue) {
        if (!safe) {
            // Remove whitespace from name and quotes the value.
            // This typically still returns bash-unsafe key-value pairs.
            return fieldKey + "=" + fieldValue;
        } else {
            // This returns bash-safe variables.
            // First, we remove any illegal characters
            fieldKey = fieldKey.replaceAll("[^a-zA-Z0-9_]+", "_");
            // In case the variable name is starting with a digit, we add in an underscore instead.
            if (fieldKey.matches("^[0-9].*$")) {
                fieldKey = "_" + fieldKey;
            }
            // All return characters should be escaped for bash
            fieldValue = fieldValue.replaceAll("\\n", "\\ \n");
            return fieldKey + "='" + fieldValue + "'";
        }
    }

    @Override
    public void writeConfiguration(Metadata metadata, File configurationFile) throws Exception {
        EnvBuilder envBuilder = this.create(metadata);
        List<String> envProperties = envBuilder.getProperties();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configurationFile));
        for (String line : envProperties) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }
}
