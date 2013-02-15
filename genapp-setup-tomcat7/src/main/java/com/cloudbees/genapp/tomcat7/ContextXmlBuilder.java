package com.cloudbees.genapp.tomcat7;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cloudbees.genapp.GenappMetadata;
import com.cloudbees.genapp.GenappMetadata.Resource;

public class ContextXmlBuilder {
    private static String[] DATASOURCE_PROPS = new String[] {
            "driverClassName", "username", "password", "url", "initialSize",
            "maxActive", "minIdle", "maxIdle", "maxWait", "validationQuery",
            "validationQueryTimeout", "testOnBorrow", "testOnReturn",
            "timeBetweenEvictionRunsMillis", "numTestsPerEvictionRun",
            "minEvictableIdleTimeMillis", "testWhileIdle", "removeAbandoned",
            "removeAbandonedTimeout", "logAbandoned", "defaultAutoCommit",
            "defaultReadOnly", "defaultTransactionIsolation",
            "poolPreparedStatements", "maxOpenPreparedStatements",
            "defaultCatalog", "connectionInitSqls", "connectionProperties",
            "accessToUnderlyingConnectionAllowed" };

    // name map for reversing toLower marshalling from the genapp env
    private static Map<String, String> dsPropNameMap = dataSourcePropsMap();

    private Document doc;
    private GenappMetadata md;

    private ContextXmlBuilder(GenappMetadata md) throws Exception {
        this.md = md;
    }

    public static ContextXmlBuilder create(GenappMetadata md) throws Exception {
        ContextXmlBuilder b = new ContextXmlBuilder(md);
        return b;
    }

    private ContextXmlBuilder addResources(GenappMetadata md) {
        for (Resource rs : md.resources.values()) {
            if (rs.type.equals(Resource.TYPE_DATABASE)) {
                addDataSource(rs);
            } else if (rs.type.equals(Resource.TYPE_MAIL)) {
                addMailSession(rs);
            }
        }

        return this;
    }

    private ContextXmlBuilder addDataSource(Resource ds) {
        Element e = doc.createElement("Resource");
        e.setAttribute("name", "jdbc/" + ds.alias);
        e.setAttribute("auth", "Container");
        e.setAttribute("type", "javax.sql.DataSource");

        for (Map.Entry<String, String> entry : ds.properties.entrySet()) {
            String resourcePropName = dsPropNameMap.containsKey(entry.getKey()) ? dsPropNameMap
                    .get(entry.getKey()) : entry.getKey();
            String value = entry.getValue();
            if (resourcePropName.equals("url")) {
                if (!value.startsWith("jdbc:")) {
                    value = "jdbc:" + value;
                }

                // set the default driverClassName for MySQL
                if (!e.hasAttribute("driverClassName")) {
                    if (value.startsWith("jdbc:mysql")) {
                        e.setAttribute("driverClassName",
                                "com.mysql.jdbc.Driver");
                    }
                }
            }
            e.setAttribute(resourcePropName, value);
        }

        doc.getDocumentElement().appendChild(e);

        return this;
    }
    
    private ContextXmlBuilder addMailSession(Resource rs) {
        Element e = doc.createElement("Resource");
        e.setAttribute("name", "mail/" + rs.alias);
        e.setAttribute("auth", "Container");
        e.setAttribute("type", "javax.mail.Session");
        
        //if there is a URL, attempt to setup the default settings
        String url = rs.properties.get("url");
        if(url != null) {
            URI uri = URI.create(url);
            e.setAttribute("mail.smtp.host", uri.getHost());
            e.setAttribute("mail.smtp.port", uri.getPort() + "");
            
            String scheme = uri.getScheme();
            if(scheme.equals("smtps")) {
                e.setAttribute("mail.smtp.ssl.enable", "true");
                e.setAttribute("mail.smtp.starttls.enable", "true");
                e.setAttribute("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                e.setAttribute("mail.smtp.socketFactory.fallback", "false");
            }
        }
        
        String username = rs.properties.get("username");
        String password = rs.properties.get("password");
        if(username != null || password != null) {
            e.setAttribute("mail.smtp.auth", "true");
        }

        for (Map.Entry<String, String> entry : rs.properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if(key.equals("username"))
                key = "mail.smtp.user";
            
            if(!key.equals("url"))
                e.setAttribute(key, value);
        }

        doc.getDocumentElement().appendChild(e);

        return this;
    }

    private static Map<String, String> dataSourcePropsMap() {
        Map<String, String> m = new HashMap<String, String>();
        for (String prop : DATASOURCE_PROPS) {
            m.put(prop.toLowerCase(), prop);
        }
        return m;
    }

    public ContextXmlBuilder fromExistingDoc(Document doc) {
        String rootElementName = doc.getDocumentElement().getNodeName();
        if (!rootElementName.equals("Context")) {
            throw new IllegalArgumentException(
                    "Document is missing root <Context> element");
        }
        this.doc = doc;
        return this;
    }

    public ContextXmlBuilder fromExistingDoc(File f) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(f);
        fromExistingDoc(doc);
        return this;
    }

    public ContextXmlBuilder fromExistingDoc(InputStream in) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(in);
        fromExistingDoc(doc);
        return this;
    }

    public Document buildDocument() throws Exception {
        if (this.doc == null) {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            this.doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Context");
            doc.appendChild(rootElement);
        }

        addResources(md);

        return doc;
    }
}
