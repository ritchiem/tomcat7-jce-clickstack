package com.cloudbees.genapp.tomcat7;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.cloudbees.genapp.GenappMetadata;
import com.cloudbees.genapp.MetadataBuilder;

public class ContextXMLTest {

    @Test
    public void testDataSources() throws Exception {
        String path = "/com/cloudbees/genapp/tomcat7/metadata-sample.json";
        InputStream in = getClass().getResourceAsStream(path);
        assertNotNull("Missing resource: " + path, in);

        GenappMetadata md = MetadataBuilder.fromStream(in);
        Document doc = ContextXmlBuilder.create(md).buildDocument();

        // dump the doc (for debugging)
        System.out.println(docAsString(doc));

        // verify that the mydb datasource is defined and matches the expected
        // XML structure
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression ds1Expr = xpath
                .compile("/Context/Resource[@name='jdbc/mydb']");
        Node node = (Node) ds1Expr.evaluate(doc, XPathConstants.NODE);
        assertEquals(8, node.getAttributes().getLength());
        assertEquals(Node.ELEMENT_NODE, ((Node) ds1Expr.evaluate(doc,
                XPathConstants.NODE)).getNodeType());
        assertEquals("Container", node.getAttributes().getNamedItem("auth")
                .getNodeValue());
        assertEquals("com.mysql.jdbc.Driver", node.getAttributes()
                .getNamedItem("driverClassName").getNodeValue());
        assertEquals("pass", node.getAttributes().getNamedItem("password")
                .getNodeValue());
        assertEquals("javax.sql.DataSource",
                node.getAttributes().getNamedItem("type").getNodeValue());
        assertEquals("jdbc:mysql://localhost:3306/my-test-db", node
                .getAttributes().getNamedItem("url").getNodeValue());
        assertEquals("10", node.getAttributes().getNamedItem("maxActive")
                .getNodeValue());

        // verify that the second datasource is also defined (checking
        // properties is verified above)
        XPathExpression expr2 = xpath
                .compile("/Context/Resource[@name='jdbc/mydb2']");
        assertEquals(Node.ELEMENT_NODE,
                ((Node) expr2.evaluate(doc, XPathConstants.NODE)).getNodeType());

        XPathExpression mailExpr = xpath
                .compile("/Context/Resource[@name='mail/SendGrid']");
        node = (Node) mailExpr.evaluate(doc, XPathConstants.NODE);
        assertEquals(Node.ELEMENT_NODE, ((Node) mailExpr.evaluate(doc,
                XPathConstants.NODE)).getNodeType());
        assertEquals(12, node.getAttributes().getLength());
        assertEquals("Container", node.getAttributes().getNamedItem("auth")
                .getNodeValue());
        assertEquals("true", node.getAttributes()
                .getNamedItem("mail.smtp.auth").getNodeValue());
        assertEquals("smtp.sendgrid.net", node.getAttributes()
                .getNamedItem("mail.smtp.host").getNodeValue());
        assertEquals("465", node.getAttributes()
                .getNamedItem("mail.smtp.port").getNodeValue());
        assertEquals("javax.net.ssl.SSLSocketFactory", node.getAttributes()
                .getNamedItem("mail.smtp.socketFactory.class").getNodeValue());
        assertEquals("false", node.getAttributes()
                .getNamedItem("mail.smtp.socketFactory.fallback").getNodeValue());
        assertEquals("true", node.getAttributes()
                .getNamedItem("mail.smtp.ssl.enable").getNodeValue());
        assertEquals("sendgrid_user", node.getAttributes()
                .getNamedItem("mail.smtp.user").getNodeValue());
        assertEquals("sendgrid_pass123", node.getAttributes()
                .getNamedItem("password").getNodeValue());
        assertEquals("javax.mail.Session",
                node.getAttributes().getNamedItem("type").getNodeValue());
    }

    @Test
    public void testExistingDoc() throws Exception {
        String metadataPath = "/com/cloudbees/genapp/tomcat7/metadata-sample.json";
        InputStream metadataIn = getClass().getResourceAsStream(metadataPath);
        assertNotNull("Missing resource: " + metadataPath, metadataIn);

        String contextXmlPath = "/com/cloudbees/genapp/tomcat7/context-sample.xml";
        InputStream contextXmlIn = getClass().getResourceAsStream(
                contextXmlPath);
        assertNotNull("Missing resource: " + contextXmlPath, contextXmlIn);

        GenappMetadata md = MetadataBuilder.fromStream(metadataIn);
        Document doc = ContextXmlBuilder.create(md)
                .fromExistingDoc(contextXmlIn).buildDocument();

        // dump the doc (for debugging)
        // System.out.println(docAsString(doc));

        // verify that the mydb datasource is defined in the updated doc
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath
                .compile("/Context/Resource[@name='jdbc/mydb']");
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());

        // verify that previously existing WatchedResource element still exists
        XPathExpression expr2 = xpath
                .compile("/Context/WatchedResource/text()");
        String watchedResource = (String) expr2.evaluate(doc,
                XPathConstants.STRING);
        assertEquals("WEB-INF/web.xml", watchedResource);
    }

    private String docAsString(Document doc)
            throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString();
        return output;
    }
}
