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

import com.cloudbees.genapp.GenappResource;
import com.cloudbees.genapp.GenappMetadata;

public class TomcatContextXmlBuilder {

    private static List<String> ADDITIONAL_DATASOURCE_PROPERTIES = Arrays.asList(
            "driverClassName", "initialSize", "maxActive", "minIdle", "maxIdle", "maxWait", "validationQuery",
            "validationQueryTimeout", "testOnBorrow", "testOnReturn", "timeBetweenEvictionRunsMillis",
            "numTestsPerEvictionRun", "minEvictableIdleTimeMillis", "testWhileIdle", "removeAbandoned",
            "removeAbandonedTimeout", "logAbandoned", "defaultAutoCommit", "defaultReadOnly",
            "defaultTransactionIsolation", "poolPreparedStatements", "maxOpenPreparedStatements", "defaultCatalog",
            "connectionInitSqls", "connectionProperties", "accessToUnderlyingConnectionAllowed");

    private Document tomcatConfigurationDocument;
    private GenappMetadata genappMetadata;

    private TomcatContextXmlBuilder(GenappMetadata genappMetadata) throws Exception {
        this.genappMetadata = genappMetadata;
    }

    public static TomcatContextXmlBuilder create(GenappMetadata genappMetadata) throws Exception {
        TomcatContextXmlBuilder tomcatContextXmlBuilder = new TomcatContextXmlBuilder(genappMetadata);
        return tomcatContextXmlBuilder;
    }

    private TomcatContextXmlBuilder addResources(GenappMetadata genappMetadata) {
        for (GenappResource genappResource : genappMetadata.getResources().values()) {
            String genappResourceType = genappResource.getType();
            addResource(genappResourceType, genappResource);
        }
        return this;
    }

    private TomcatContextXmlBuilder addResource(String genappResourceType, GenappResource genappResource) {
        if (genappResourceType.equals("database") || genappResourceType.equals("datasource"))
            return addDatasource(genappResource);
        else if (genappResourceType.equals("email"))
            return addMailResource(genappResource);
        else
            return this;
    }

    private TomcatContextXmlBuilder addDatasource(GenappResource genappDatasource) {
        Element tomcatDatasourceConfigurationElement = tomcatConfigurationDocument.createElement("Resource");
        tomcatDatasourceConfigurationElement.setAttribute("name", "jdbc/" + genappDatasource.getName());
        tomcatDatasourceConfigurationElement.setAttribute("auth", "Container");
        tomcatDatasourceConfigurationElement.setAttribute("type", "javax.sql.DataSource");

        for (Map.Entry<String, String> genappDatasourcePropertyEntry : genappDatasource.getProperties().entrySet()) {
            String genappDatasourcePropertyKey = genappDatasourcePropertyEntry.getKey();
            String genappDatasourcePropertyValue = genappDatasourcePropertyEntry.getValue();

            // Translate the basic parameters into java-compatible format.
            if (genappDatasourcePropertyKey.equals("DATABASE_URL")) {

                // Convert to jdbc format
                if (!genappDatasourcePropertyValue.startsWith("jdbc:")) {
                    genappDatasourcePropertyValue = "jdbc:" + genappDatasourcePropertyValue;
                }
                tomcatDatasourceConfigurationElement.setAttribute("url", genappDatasourcePropertyValue);

                // Guess the right driver to use.
                if (!genappDatasource.getProperties().containsKey("driverClassName")) {
                    String driver;

                    if (genappDatasourcePropertyValue.startsWith("jdbc:postgresql")) {
                        driver = "org.postgresql.Driver";
                    } else if (genappDatasourcePropertyValue.startsWith("jdbc:jtds")) {
                        driver = "net.sourceforge.jtds.jdbc.Driver";
                    } else if (genappDatasourcePropertyValue.startsWith("jdbc:microsoft:sqlserver")) {
                        driver = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
                    } else if (genappDatasourcePropertyValue.startsWith("jdbc:oracle:thin")) {
                        driver = "oracle.jdbc.driver.OracleDriver";
                    } else {
                        driver = "com.mysql.jdbc.Driver";
                    }
                    tomcatDatasourceConfigurationElement.setAttribute("driverClassName", driver);
                }
            } else if (genappDatasourcePropertyKey.equals("DATABASE_USERNAME")) {
                tomcatDatasourceConfigurationElement.setAttribute("username", genappDatasourcePropertyValue);
            } else if (genappDatasourcePropertyKey.equals("DATABASE_PASSWORD")) {
                tomcatDatasourceConfigurationElement.setAttribute("password", genappDatasourcePropertyValue);
            } else if (ADDITIONAL_DATASOURCE_PROPERTIES.contains(genappDatasourcePropertyKey)) {
                tomcatDatasourceConfigurationElement
                        .setAttribute(genappDatasourcePropertyKey, genappDatasourcePropertyValue);
            }
        }

        tomcatConfigurationDocument.getDocumentElement().appendChild(tomcatDatasourceConfigurationElement);
        return this;
    }
    
    private TomcatContextXmlBuilder addMailResource(GenappResource genappEmailResource) {
        Element tomcatMailConfigurationElement = tomcatConfigurationDocument.createElement("Resource");
        tomcatMailConfigurationElement.setAttribute("name", "mail/" + genappEmailResource.getName());
        tomcatMailConfigurationElement.setAttribute("auth", "Container");
        tomcatMailConfigurationElement.setAttribute("type", "javax.mail.Session");
        tomcatMailConfigurationElement
                .setAttribute("mail.smtp.user", genappEmailResource.getProperties().get("SENDGRID_USERNAME"));
        tomcatMailConfigurationElement
                .setAttribute("mail.smtp.password", genappEmailResource.getProperties().get("SENDGRID_PASSWORD"));
        tomcatMailConfigurationElement
                .setAttribute("mail.smtp.host", genappEmailResource.getProperties().get("SENDGRID_SMTP_HOST"));
        tomcatMailConfigurationElement.setAttribute("mail.smtp.auth", "true");
        tomcatConfigurationDocument.getDocumentElement().appendChild(tomcatMailConfigurationElement);
        return this;
    }

    public TomcatContextXmlBuilder fromExistingDoc(Document configurationDocument) {
        String rootElementName = configurationDocument.getDocumentElement().getNodeName();
        if (!rootElementName.equals("Context"))
            throw new IllegalArgumentException("Document is missing root <Context> element");
        this.tomcatConfigurationDocument = configurationDocument;
        return this;
    }

    public TomcatContextXmlBuilder fromExistingDoc(File file)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        fromExistingDoc(document);
        return this;
    }

    public TomcatContextXmlBuilder fromExistingDoc(InputStream inputStream) throws Exception {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document configurationDocument = documentBuilder.parse(inputStream);
        fromExistingDoc(configurationDocument);
        return this;
    }

    public Document buildConfigurationDocument() throws ParserConfigurationException {
        if (tomcatConfigurationDocument == null) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =documentBuilderFactory.newDocumentBuilder();
            tomcatConfigurationDocument = documentBuilder.newDocument();
            Element rootConfigurationDocumentElement = tomcatConfigurationDocument.createElement("Context");
            tomcatConfigurationDocument.appendChild(rootConfigurationDocumentElement);
        }

        addResources(genappMetadata);
        return tomcatConfigurationDocument;
    }
}
