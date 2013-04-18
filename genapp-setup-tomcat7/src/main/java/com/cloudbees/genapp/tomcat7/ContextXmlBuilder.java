package com.cloudbees.genapp.tomcat7;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import com.cloudbees.genapp.Metadata;
import com.cloudbees.genapp.Resource;

public class ContextXmlBuilder {

    private static List<String> ADDITIONAL_DATASOURCE_PROPERTIES = Arrays.asList(
            "driverClassName", "initialSize", "maxActive", "minIdle", "maxIdle", "maxWait", "validationQuery",
            "validationQueryTimeout", "testOnBorrow", "testOnReturn", "timeBetweenEvictionRunsMillis",
            "numTestsPerEvictionRun", "minEvictableIdleTimeMillis", "testWhileIdle", "removeAbandoned",
            "removeAbandonedTimeout", "logAbandoned", "defaultAutoCommit", "defaultReadOnly",
            "defaultTransactionIsolation", "poolPreparedStatements", "maxOpenPreparedStatements", "defaultCatalog",
            "connectionInitSqls", "connectionProperties", "accessToUnderlyingConnectionAllowed");

    private Document contextDocument;
    private Metadata metadata;

    private ContextXmlBuilder(Metadata metadata) {
        this.metadata = metadata;
    }

    public static ContextXmlBuilder create(Metadata metadata) {
        return new ContextXmlBuilder(metadata);
    }

    private ContextXmlBuilder addResources(Metadata metadata) {
        for (Resource resource : metadata.getResources().values()) {
            String resourceType = resource.getType();
            addResource(resourceType, resource);
        }
        return this;
    }

    private ContextXmlBuilder addResource(String resourceType, Resource resource) {
        if (resourceType.equals("database") || resourceType.equals("datasource"))
            return addDatasource(resource);
        else if (resourceType.equals("email"))
            return addMailResource(resource);
        else
            return this;
    }

    private ContextXmlBuilder addDatasource(Resource datasource) {
        Element datasourceContextElement = contextDocument.createElement("Resource");
        datasourceContextElement.setAttribute("name", "jdbc/" + datasource.getName());
        datasourceContextElement.setAttribute("auth", "Container");
        datasourceContextElement.setAttribute("type", "javax.sql.DataSource");

        for (Map.Entry<String, String> datasourcePropertyEntry : datasource.getProperties().entrySet()) {
            String datasourcePropertyKey = datasourcePropertyEntry.getKey();
            String datasourcePropertyValue = datasourcePropertyEntry.getValue();

            // Translate the basic parameters into java-compatible format.
            if (datasourcePropertyKey.equals("DATABASE_URL")) {

                // Convert to jdbc format
                if (!datasourcePropertyValue.startsWith("jdbc:")) {
                    datasourcePropertyValue = "jdbc:" + datasourcePropertyValue;
                }
                datasourceContextElement.setAttribute("url", datasourcePropertyValue);

                // Guess the right driver to use.
                if (!datasource.getProperties().containsKey("driverClassName")) {
                    String driver;

                    if (datasourcePropertyValue.startsWith("jdbc:postgresql")) {
                        driver = "org.postgresql.Driver";
                    } else if (datasourcePropertyValue.startsWith("jdbc:jtds")) {
                        driver = "net.sourceforge.jtds.jdbc.Driver";
                    } else if (datasourcePropertyValue.startsWith("jdbc:microsoft:sqlserver")) {
                        driver = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
                    } else if (datasourcePropertyValue.startsWith("jdbc:oracle:thin")) {
                        driver = "oracle.jdbc.driver.OracleDriver";
                    } else {
                        driver = "com.mysql.jdbc.Driver";
                    }
                    datasourceContextElement.setAttribute("driverClassName", driver);
                }
            } else if (datasourcePropertyKey.equals("DATABASE_USERNAME")) {
                datasourceContextElement.setAttribute("username", datasourcePropertyValue);
            } else if (datasourcePropertyKey.equals("DATABASE_PASSWORD")) {
                datasourceContextElement.setAttribute("password", datasourcePropertyValue);
            } else if (ADDITIONAL_DATASOURCE_PROPERTIES.contains(datasourcePropertyKey)) {
                datasourceContextElement.setAttribute(datasourcePropertyKey, datasourcePropertyValue);
            }
        }

        contextDocument.getDocumentElement().appendChild(datasourceContextElement);
        return this;
    }
    
    private ContextXmlBuilder addMailResource(Resource mailResource) {
        Element mailContextElement = contextDocument.createElement("Resource");
        mailContextElement.setAttribute("name", "mail/" + mailResource.getName());
        mailContextElement.setAttribute("auth", "Container");
        mailContextElement.setAttribute("type", "javax.mail.Session");
        mailContextElement
                .setAttribute("mail.smtp.user", mailResource.getProperties().get("SENDGRID_USERNAME"));
        mailContextElement
                .setAttribute("mail.smtp.password", mailResource.getProperties().get("SENDGRID_PASSWORD"));
        mailContextElement
                .setAttribute("mail.smtp.host", mailResource.getProperties().get("SENDGRID_SMTP_HOST"));
        mailContextElement.setAttribute("mail.smtp.auth", "true");
        contextDocument.getDocumentElement().appendChild(mailContextElement);
        return this;
    }

    public ContextXmlBuilder fromExistingDoc(Document contextDocument) {
        String rootElementName = contextDocument.getDocumentElement().getNodeName();
        if (!rootElementName.equals("Context"))
            throw new IllegalArgumentException("Document is missing root <Context> element");
        this.contextDocument = contextDocument;
        return this;
    }

    public ContextXmlBuilder fromExistingDoc(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        fromExistingDoc(document);
        return this;
    }

    public ContextXmlBuilder fromExistingDoc(InputStream inputStream)
            throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document contextDocument = documentBuilder.parse(inputStream);
        fromExistingDoc(contextDocument);
        return this;
    }

    public Document buildContextDocument() throws ParserConfigurationException {
        if (contextDocument == null) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =documentBuilderFactory.newDocumentBuilder();
            contextDocument = documentBuilder.newDocument();
            Element rootContextDocumentElement = contextDocument.createElement("Context");
            contextDocument.appendChild(rootContextDocumentElement);
        }

        addResources(metadata);
        return contextDocument;
    }
}
