package com.cloudbees.genapp;

import static org.junit.Assert.*;

import java.io.InputStream;
import org.junit.Test;

import com.cloudbees.genapp.GenappMetadata;
import com.cloudbees.genapp.MetadataBuilder;

public class MetadataTest {

    @Test
    public void loadMetadata() throws Exception {
        String path = "/com/cloudbees/genapp/tomcat7/metadata-sample.json";
        InputStream in = getClass().getResourceAsStream(path);
        assertNotNull("Missing resource: " + path, in);

        GenappMetadata md = MetadataBuilder.fromStream(in);
        
        assertEquals(md.appEnv.size(), 8);
        assertEquals("pass", md.appEnv.get("MYSQL_PASSWORD_MYDB"));
        assertEquals("10", md.appEnv.get("MYSQL_MAXACTIVE_MYDB"));
        assertEquals("test-db", md.appEnv.get("MYSQL_USERNAME_MYDB"));
        assertEquals("mysql://localhost:3306/my-test-db", md.appEnv.get("MYSQL_URL_MYDB"));
        assertEquals("pass", md.appEnv.get("MYSQL_PASSWORD_MYDB2"));
        assertEquals("test2-db", md.appEnv.get("MYSQL_USERNAME_MYDB2"));
        assertEquals("mysql://localhost:3306/my-test2-db", md.appEnv.get("MYSQL_URL_MYDB2"));
        assertEquals("configVal1", md.appEnv.get("configVar1"));
        
        assertEquals(md.datasources.size(), 2);
        GenappMetadata.DataSource ds = new GenappMetadata.DataSource();
        ds.alias = "mydb";
        ds.properties.put("password", "pass");
        ds.properties.put("maxactive", "10");
        ds.properties.put("username", "test-db");
        ds.properties.put("url", "mysql://localhost:3306/my-test-db");
        assertEquals(ds, md.datasources.get("mydb"));
        
        GenappMetadata.DataSource ds2 = new GenappMetadata.DataSource();
        ds2.alias = "mydb2";
        ds2.properties.put("password", "pass");
        ds2.properties.put("username", "test2-db");
        ds2.properties.put("url", "mysql://localhost:3306/my-test2-db");
        assertEquals(ds2, md.datasources.get("mydb2"));
    }
}
