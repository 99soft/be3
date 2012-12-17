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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TypeHandlerRegistryTestCase {

	private TypeHandlerRegistry typeHandlerRegistry;

	@BeforeTest
	public void setUp() throws TypeHandlerRegistryException {
		typeHandlerRegistry = new TypeHandlerRegistry();
		final URIResourceTypeHandler uriResourceTypeHandler = new URIResourceTypeHandler();
		final StringValueTypeHandler stringValueTypeHandler = new StringValueTypeHandler();
		final IntegerValueTypeHandler integerValueTypeHandler = new IntegerValueTypeHandler();
		final DoubleValueTypeHandler doubleValueTypeHandler = new DoubleValueTypeHandler();

		final URLResourceTypeHandler urlResourceTypeHandler = new URLResourceTypeHandler();
		typeHandlerRegistry.registerTypeHandler(uriResourceTypeHandler,
				URI.class, XMLSchema.ANYURI);
		typeHandlerRegistry.registerTypeHandler(stringValueTypeHandler,
				String.class, XMLSchema.STRING);
		typeHandlerRegistry.registerTypeHandler(integerValueTypeHandler,
				Integer.class, XMLSchema.INTEGER);
		typeHandlerRegistry.registerTypeHandler(urlResourceTypeHandler,
				URL.class, XMLSchema.ANYURI);
		typeHandlerRegistry.registerTypeHandler(doubleValueTypeHandler,
				Double.class, XMLSchema.DOUBLE);
	}

	@AfterTest
	public void tearDown() {
		typeHandlerRegistry = null;
	}

	@Test
	public void uriTest() throws URISyntaxException, TypeHandlerException,
			TypeHandlerRegistryException {
		final ResourceTypeHandler<URI> resourceTypeHandler = (ResourceTypeHandler<URI>) typeHandlerRegistry
				.getTypeHandler(URI.class);
		final URI actual = new URI("http://davidepalmisano.com");
		final Resource resource = resourceTypeHandler.serialize(actual);
		final URI expected = resourceTypeHandler.deserialize(resource);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void urlTest() throws TypeHandlerException, MalformedURLException,
			TypeHandlerRegistryException {
		final ResourceTypeHandler<URL> resourceTypeHandler = (ResourceTypeHandler<URL>) typeHandlerRegistry
				.getTypeHandler(URL.class);
		final URL actual = new URL("http://davidepalmisano.com");
		final Resource resource = resourceTypeHandler.serialize(actual);
		final URL expected = resourceTypeHandler.deserialize(resource);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void stringTest() throws TypeHandlerException,
			MalformedURLException, TypeHandlerRegistryException {
		final ValueTypeHandler<String> valueTypeHandler = (ValueTypeHandler<String>) typeHandlerRegistry
				.getTypeHandler(String.class);
		final String actual = "just a test string";
		final Value value = valueTypeHandler.serialize(actual);
		final String expected = valueTypeHandler.deserialize(value);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void integerTest() throws TypeHandlerException,
			MalformedURLException, TypeHandlerRegistryException {
		final ValueTypeHandler<Integer> valueTypeHandler = (ValueTypeHandler<Integer>) typeHandlerRegistry
				.getTypeHandler(Integer.class);
		final Integer actual = new Integer(1000);
		final Value value = valueTypeHandler.serialize(actual);
		final Integer expected = valueTypeHandler.deserialize(value);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void doubleTest() throws TypeHandlerException,
			MalformedURLException, TypeHandlerRegistryException {
		final ValueTypeHandler<Double> valueTypeHandler = (ValueTypeHandler<Double>) typeHandlerRegistry
				.getTypeHandler(Double.class);
		final Double actual = new Double(1.34);
		final Value value = valueTypeHandler.serialize(actual);
		final Double expected = valueTypeHandler.deserialize(value);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void integratedTest() throws URISyntaxException,
			TypeHandlerRegistryException {
		final URI actual = new URI("http://davidepalmisano.com");
		final Statement statement = typeHandlerRegistry.getStatement(
				new URIImpl("http://fake.com/user/1"), new URIImpl(
						"http://onto.it/homepage"), new URI(
						"http://davidepalmisano.com"));
		final URI expected = (URI) typeHandlerRegistry.getObject(statement);
		Assert.assertTrue(expected instanceof URI);
		Assert.assertEquals(actual, expected);
	}

}
