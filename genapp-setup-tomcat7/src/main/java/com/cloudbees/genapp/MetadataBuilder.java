package com.cloudbees.genapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudbees.genapp.GenappMetadata.DataSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetadataBuilder {
    private GenappMetadata md;

    private MetadataBuilder() {
        md = new GenappMetadata();
    }

    private MetadataBuilder(GenappMetadata md) {
        this.md = md;
    }

    private MetadataBuilder(MetadataBuilder mb) {
        md = mb.md;
    }

    public static GenappMetadata fromFile(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        try {
            return fromStream(in);
        } finally {
            if (in != null)
                in.close();
        }
    }

    public static GenappMetadata fromStream(InputStream in) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = mapper.readTree(in);
        MetadataBuilder mb = new MetadataBuilder();

        JsonNode appNode = node.findValue("app");
        if (appNode != null) {
            JsonNode envNode = appNode.findValue("env");
            if (envNode != null)
                mb = mb.buildEnv(envNode);
        }

        mb = mb.buildDataSourcesFromEnv(mb.md.appEnv);

        return mb.md;
    }

    private MetadataBuilder buildEnv(JsonNode envNode) {
        Map<String, String> envs = new TreeMap<String, String>(md.appEnv);
        for (Iterator<Map.Entry<String, JsonNode>> fields = envNode.fields(); fields
                .hasNext();) {
            Map.Entry<String, JsonNode> env = fields.next();
            envs.put(env.getKey(), env.getValue().asText());
        }
        return new MetadataBuilder(this.md.addEnvironment(envs));
    }

    private MetadataBuilder buildDataSourcesFromEnv(Map<String, String> envs)
            throws Exception {
        Map<String, DataSource> datasources = new TreeMap<String, DataSource>();
        for (Map.Entry<String, String> e : envs.entrySet()) {
            Matcher m = mysqlTuple.matcher(e.getKey());
            if (m.matches()) {
                String property = m.group(1).toLowerCase();
                String alias = m.group(2).toLowerCase();
                DataSource ds = datasources.get(alias);
                if (ds == null) {
                    ds = new DataSource();
                    ds.alias = alias;
                    datasources.put(ds.alias, ds);
                }
                ds.properties.put(property, e.getValue());
            }
        }

        return new MetadataBuilder(this.md.addDataSources(datasources));
    }

    private Pattern mysqlTuple = Pattern.compile("MYSQL_(.+)_([^_]+)");
}
