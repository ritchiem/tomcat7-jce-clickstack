package com.cloudbees.genapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudbees.genapp.GenappMetadata.Resource;
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

        mb = mb.buildResourcesFromEnv(mysqlTuple, Resource.TYPE_DATABASE);
        mb = mb.buildResourcesFromEnv(mailTuple, Resource.TYPE_MAIL);
        mb = mb.buildSendGridResourcesFromEnv();

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

    /**
     * Injects the old-style (deprecated) SendGrid resource.
     */
    private MetadataBuilder buildSendGridResourcesFromEnv() throws Exception {
        Map<String, String> envs = md.appEnv;
        if (envs.containsKey("SENDGRID_SMTP_HOST")) {
            String host = envs.get("SENDGRID_SMTP_HOST");
            String user = envs.get("SENDGRID_USERNAME");
            String password = envs.get("SENDGRID_PASSWORD");
            Resource r = new Resource("SendGrid", Resource.TYPE_MAIL);
            r.properties.put("url", "smtps://" + host + ":" + 465 + "/");
            r.properties.put("username", user);
            r.properties.put("password", password);
            return new MetadataBuilder(md.addResource(r));
        }
        return this;
    }

    private MetadataBuilder buildResourcesFromEnv(Pattern pattern,
            String resourceType) throws Exception {
        Map<String, String> envs = md.appEnv;
        Map<String, Resource> resources = new TreeMap<String, Resource>();
        for (Map.Entry<String, String> e : envs.entrySet()) {
            Matcher m = pattern.matcher(e.getKey());
            if (m.matches()) {
                String property = m.group(1).toLowerCase();
                String alias = m.group(2).toLowerCase();
                Resource ds = resources.get(alias);
                if (ds == null) {
                    ds = new Resource(alias, resourceType);
                    resources.put(ds.alias, ds);
                }
                ds.properties.put(property, e.getValue());
            }
        }

        return new MetadataBuilder(md.addResources(resources));
    }

    private static Pattern mysqlTuple = Pattern.compile("MYSQL_(.+)_([^_]+)");
    private static Pattern mailTuple = Pattern.compile("MAIL_(.+)_([^_]+)");
}
