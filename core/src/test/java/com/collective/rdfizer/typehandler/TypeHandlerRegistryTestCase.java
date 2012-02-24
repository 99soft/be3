package com.collective.rdfizer.typehandler;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TypeHandlerRegistryTestCase {

    private TypeHandlerRegistry typeHandlerRegistry;

    @BeforeTest
    public void setUp() throws TypeHandlerRegistryException {
        typeHandlerRegistry = new TypeHandlerRegistry();
        URIResourceTypeHandler uriResourceTypeHandler = new URIResourceTypeHandler();
        StringValueTypeHandler stringValueTypeHandler = new StringValueTypeHandler();
        IntegerValueTypeHandler integerValueTypeHandler = new IntegerValueTypeHandler();
        URLResourceTypeHandler urlResourceTypeHandler = new URLResourceTypeHandler();
        typeHandlerRegistry.registerTypeHandler(uriResourceTypeHandler, URI.class, XMLSchema.ANYURI);
        typeHandlerRegistry.registerTypeHandler(stringValueTypeHandler, String.class, XMLSchema.STRING);
        typeHandlerRegistry.registerTypeHandler(integerValueTypeHandler, Integer.class, XMLSchema.INTEGER);
        typeHandlerRegistry.registerTypeHandler(urlResourceTypeHandler, URL.class, XMLSchema.ANYURI);
    }

    @AfterTest
    public void tearDown() {
        typeHandlerRegistry = null;
    }

    @Test
    public void uriTest() throws URISyntaxException, TypeHandlerException,
            TypeHandlerRegistryException {
        ResourceTypeHandler<URI> resourceTypeHandler =
                (ResourceTypeHandler<URI>) typeHandlerRegistry.getTypeHandler(URI.class);
        URI actual = new URI("http://davidepalmisano.com");
        Resource resource = resourceTypeHandler.serialize(actual);
        URI expected = resourceTypeHandler.deserialize(resource);
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void urlTest() throws TypeHandlerException, MalformedURLException,
            TypeHandlerRegistryException {
        ResourceTypeHandler<URL> resourceTypeHandler =
                (ResourceTypeHandler<URL>) typeHandlerRegistry.getTypeHandler(URL.class);
        URL actual = new URL("http://davidepalmisano.com");
        Resource resource = resourceTypeHandler.serialize(actual);
        URL expected = resourceTypeHandler.deserialize(resource);
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void stringTest() throws TypeHandlerException, MalformedURLException,
            TypeHandlerRegistryException {
        ValueTypeHandler<String> valueTypeHandler =
                (ValueTypeHandler<String>) typeHandlerRegistry.getTypeHandler(String.class);
        String actual = "just a test string";
        Value value = valueTypeHandler.serialize(actual);
        String expected = valueTypeHandler.deserialize(value);
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void integerTest() throws TypeHandlerException, MalformedURLException,
            TypeHandlerRegistryException {
        ValueTypeHandler<Integer> valueTypeHandler =
                (ValueTypeHandler<Integer>) typeHandlerRegistry.getTypeHandler(Integer.class);
        Integer actual = new Integer(1000);
        Value value = valueTypeHandler.serialize(actual);
        Integer expected = valueTypeHandler.deserialize(value);
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void integratedTest() throws URISyntaxException, TypeHandlerRegistryException {
        URI actual = new URI("http://davidepalmisano.com");
        Statement statement = typeHandlerRegistry.getStatement(
                new URIImpl("http://fake.com/user/1"),
                new URIImpl("http://onto.it/homepage"),
                new URI("http://davidepalmisano.com")
        );
        URI expected = (URI) typeHandlerRegistry.getObject(statement);
        Assert.assertTrue(expected instanceof URI);
        Assert.assertEquals(actual, expected);
    }

}
