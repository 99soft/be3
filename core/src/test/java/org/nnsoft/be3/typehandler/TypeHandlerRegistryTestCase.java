/*
 * Copyright (c) 2012 The 99 Software Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.nnsoft.be3.typehandler;

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
