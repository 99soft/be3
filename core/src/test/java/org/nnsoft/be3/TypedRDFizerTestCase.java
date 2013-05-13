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
package org.nnsoft.be3;

import static org.nnsoft.be3.model.nested.Author.AuthorBuilder.author;
import static org.nnsoft.be3.model.nested.Book.BookBuilder.book;
import static org.nnsoft.be3.model.nested.Page.PageBuilder.page;
import static org.nnsoft.be3.model.nested.SimpleBook.SimpleBookBuilder.simpleBook;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.nnsoft.be3.model.Person;
import org.nnsoft.be3.model.hierarchy.EnhancedResource;
import org.nnsoft.be3.model.namespace.Polarity;
import org.nnsoft.be3.model.nested.Book;
import org.nnsoft.be3.model.nested.Page;
import org.nnsoft.be3.model.nested.SimpleBook;
import org.nnsoft.be3.typehandler.TypeHandlerRegistryException;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Reference test class for {@link DefaultTypedBe3Impl}.
 * 
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
public class TypedRDFizerTestCase {
    
    private TypedBe3 b3;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TypedRDFizerTestCase.class);
    
    public static Person getPerson() throws URISyntaxException, ParseException {
    
        final Person dpalmisano = new Person("Davide", "Palmisano");
        dpalmisano.addTag("Semantic Web");
        dpalmisano.addTag("Java");
        dpalmisano.addTag("Linked data");
        dpalmisano.setBirthDate(new Date());
        dpalmisano.addConcept(new java.net.URI("http://dbpedia.org/resource/Italy"));
        dpalmisano.addConcept(new java.net.URI("http://dbpedia.org/resource/London"));
        final Person mox601 = new Person("Matteo", "Moci");
        mox601.addTag("Java");
        mox601.addTag("Apache Maven");
        mox601.setBirthDate(new Date());
        dpalmisano.addKnow(mox601);
        return dpalmisano;
    }
    
    @BeforeMethod
    public void setUp() throws RepositoryException, TypeHandlerRegistryException {
    
        b3 = new DefaultTypedBe3Impl();
    }
    
    @AfterMethod
    public void tearDown() {
    
        b3 = null;
    }
    
    @Test
    public void testSerialization() throws ParseException, URISyntaxException, RDFizerException {
    
        b3.serialize(getPerson(), System.out, Format.RDFXML);
    }
    
    @Test
    public void testGetRDFStatements() throws RDFizerException, URISyntaxException, ParseException {
    
        final List<Statement> statements = b3.getRDFStatements(getPerson());
        Assert.assertNotNull(statements);
        System.out.println(statements.size());
        assertTrue(statements.size() == 20);
        for (final Statement statement : statements) {
            System.out.println(statement);
        }
    }
    
    @Test
    public void cantRegisterProtectedXMLSchemaTypeHandlers() {
    
        // URI and URL are "protected" classes
        try {
            b3.registerTypeHandler(null, URI.class, XMLSchema.ANYURI);
        } catch (final RDFizerException e) {
            LOGGER.info("correctly caught exception: '" + e.getMessage() + "'");
            assertNotNull(e);
        }
        
    }
    
    // TODO: this test fails for the dateTypeHandler
    @Test(enabled = false)
    public void testGetPersonObject() throws URISyntaxException, RDFizerException, ParseException {
    
        final Person person = getPerson();
        final List<Statement> statements = b3.getRDFStatements(person);
        final Person retrievedPerson = b3
                .getObject(statements, new URIImpl(b3.getIdentifierPrefix(Person.class).toString()
                        + "/" + person.getId()), Person.class);
        assertNotNull(retrievedPerson);
        assertEquals(person, retrievedPerson);
        assertEquals(person.getName(), retrievedPerson.getName());
        assertEquals(person.getSurname(), retrievedPerson.getSurname());
        assertEquals(person.getBirthDate().toString(), retrievedPerson.getBirthDate().toString());
        assertEqualsNoOrder(person.getConcepts().toArray(), retrievedPerson.getConcepts().toArray());
        assertEqualsNoOrder(person.getKnows().toArray(), retrievedPerson.getKnows().toArray());
        assertEqualsNoOrder(person.getTags().toArray(), retrievedPerson.getTags().toArray());
    }
    
    @Test
    public void shouldSerializeNestedObjects() throws RDFizerException {
    
        final Book book = getBook();
        final List<Statement> statements = b3.getRDFStatements(book);
        final Comparator<Statement> statementComparator = new StatementComparator();
        Collections.sort(statements, statementComparator);
        
        final Book retrievedBook = b3.getObject(statements,
                new URIImpl(b3.getIdentifierPrefix(Book.class).toString() + "/" + book.getId()),
                Book.class);
        
        assertEquals(statements.size(), 8);
        assertTrue(retrievedBook.getId().equals(book.getId()));
        assertTrue(retrievedBook.getTitle().equals(book.getTitle()));
        assertEqualsNoOrder(retrievedBook.getPages().toArray(), book.getPages().toArray());
    }
    
    @Test
    public void shouldSerializeSimpleNestedObjects() throws RDFizerException {
    
        final SimpleBook simpleBook = getSimpleBook();
        final List<Statement> statements = b3.getRDFStatements(simpleBook);
        
        final Comparator<Statement> statementComparator = new StatementComparator();
        Collections.sort(statements, statementComparator);
        
        // for (Statement st : statements) {
        // logger.debug("simpleBook statement: " + st);
        // }
        
        assertEquals(statements.size(), 9);
        // TODO: test more things?
    }
    
    @Test
    public void shouldAnnotateChildClassAndNotExtendedClass() throws RDFizerException {
    
        final EnhancedResource enhancedResource = getEnhancedResource();
        final List<Statement> statements = b3.getRDFStatements(enhancedResource);
        
        assertTrue(statements.size() == 4);
        
        final EnhancedResource retrievedEnhancedResource = b3.getObject(statements,
                new URIImpl(b3.getIdentifierPrefix(EnhancedResource.class).toString() + "/"
                        + enhancedResource.getId()), EnhancedResource.class);
        assertNotNull(retrievedEnhancedResource);
        assertTrue(enhancedResource.getId().compareTo(retrievedEnhancedResource.getId()) == 0);
        assertTrue(enhancedResource.getTitle().equals(retrievedEnhancedResource.getTitle()));
        assertEqualsNoOrder(enhancedResource.getTopics().toArray(), retrievedEnhancedResource
                .getTopics().toArray());
    }
    
    @Test
    public void shouldSerializeObjectSkippingNullFields() throws RDFizerException {
    
        final Long pageId = 45L;
        final Page pageWithNullContent = Page.PageBuilder.page().withNumber(pageId)
                .havingContent(null).build();
        assertNull(pageWithNullContent.getContent());
        b3.serialize(pageWithNullContent, System.out, Format.RDFXML);
        final List<Statement> statements = b3.getRDFStatements(pageWithNullContent);
        
        final Page retrievedPageWithNullField = b3.getObject(statements,
                new URIImpl(b3.getIdentifierPrefix(Page.class).toString() + "/"
                        + pageWithNullContent.getNumber()), Page.class);
        
        assertNull(retrievedPageWithNullField.getContent());
        assertEquals(retrievedPageWithNullField.getNumber(), pageId);
    }
    
    @Test
    public void shouldSerializeObjectWithCustomNamespace() throws RDFizerException {
    
        final Polarity negativePolarity = new Polarity("negative");
        b3.serialize(negativePolarity, System.out, Format.RDFXML);
        LOGGER.info("");
        final List<Statement> statements = b3.getRDFStatements(negativePolarity);
        
        for (final Statement stmt : statements) {
            LOGGER.info(stmt.toString());
        }
        
        final Polarity deserialisedPolarity = b3.getObject(statements, new URIImpl(b3
                .getIdentifierPrefix(Polarity.class).toString() + "/" + negativePolarity.getId()),
                Polarity.class);
        assertEquals(deserialisedPolarity, negativePolarity);
    }
    
    private EnhancedResource getEnhancedResource() {
    
        final EnhancedResource enhancedResource = new EnhancedResource();
        enhancedResource.setId(new Long(1));
        enhancedResource.setTitle("this is a fake title");
        final List<java.net.URI> topics = new ArrayList<java.net.URI>();
        try {
            topics.add(new java.net.URI("http://first.uri/com"));
            topics.add(new java.net.URI("http://second.uri/com"));
        } catch (final URISyntaxException e) {
            // should never happen
        }
        enhancedResource.setTopics(topics);
        return enhancedResource;
    }
    
    public SimpleBook getSimpleBook() {
    
        final SimpleBook simpleBook = simpleBook().withId(1)
                .withMainAuthor(author().withId(1).build()).addAuthor(author().withId(2).build())
                .addAuthor(author().withId(3).build()).addAuthor(author().withId(4).build())
                .build();
        return simpleBook;
    }
    
    public Book getBook() {
    
        final Book book = book()
                .withId(new Long(1))
                .withTitle("book title")
                .havingPage(
                        page().withNumber(new Long(1)).havingContent("first page content").build())
                .havingPage(
                        page().withNumber(new Long(2)).havingContent("second page content").build())
                .build();
        
        return book;
    }
    
}