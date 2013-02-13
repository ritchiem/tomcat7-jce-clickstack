package com.cloudbees.genapp.tomcat7;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cloudbees.genapp.GenappMetadata;
import com.cloudbees.genapp.GenappMetadata.DataSource;

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

    private ContextXmlBuilder addDataSources(GenappMetadata md) {
        for (DataSource ds : md.datasources.values()) {
            addDataSource(ds);
        }

        return this;
    }

    private ContextXmlBuilder addDataSource(DataSource ds) {
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

    private static Map<String, String> dataSourcePropsMap() {
        Map<String, String> m = new HashMap<String, String>();
        for (String prop : DATASOURCE_PROPS) {
            m.put(prop.toLowerCase(), prop);
        }
        return m;
    }
    
    public ContextXmlBuilder fromExistingDoc(Document doc) {
        String rootElementName = doc.getDocumentElement().getNodeName();
        if(!rootElementName.equals("Context")) {
            throw new IllegalArgumentException("Document is missing root <Context> element");
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

        addDataSources(md);

        return doc;
    }
}
