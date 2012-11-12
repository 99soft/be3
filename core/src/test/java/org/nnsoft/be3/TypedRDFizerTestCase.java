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
import static org.nnsoft.be3.model.nested.Page.PageBuilder.page;
import static org.testng.Assert.*;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.model.Person;
import org.nnsoft.be3.model.hierarchy.EnhancedResource;
import org.nnsoft.be3.model.nested.Book;
import org.nnsoft.be3.model.nested.Page;
import org.nnsoft.be3.model.nested.SimpleBook;
import static org.nnsoft.be3.model.nested.SimpleBook.SimpleBookBuilder.simpleBook;
import static org.nnsoft.be3.model.nested.Book.BookBuilder.book;
import org.nnsoft.be3.typehandler.*;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * Reference test class for {@link DefaultTypedBe3Impl}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
public class TypedRDFizerTestCase {

    private Be3 b3;

    private Repository repository;

    public static Person getPerson() throws URISyntaxException, ParseException {
        Person dpalmisano = new Person("Davide", "Palmisano");
        dpalmisano.addTag("Semantic Web");
        dpalmisano.addTag("Java");
        dpalmisano.addTag("Linked data");
        dpalmisano.setBirthDate(new Date());
        dpalmisano.addConcept(new java.net.URI("http://dbpedia.org/resource/Italy"));
        dpalmisano.addConcept(new java.net.URI("http://dbpedia.org/resource/London"));
        Person mox601 = new Person("Matteo", "Moci");
        mox601.addTag("Java");
        mox601.addTag("Apache Maven");
        mox601.setBirthDate(new Date());
        dpalmisano.addKnow(mox601);
        return dpalmisano;
    }

    public static URI getIdentifierURI(Class clazz) {
        RDFClassType rdfClassType = (RDFClassType) clazz.getAnnotation(RDFClassType.class);
        return new URIImpl(rdfClassType.type());
    }

    @BeforeTest
    public void setUp() throws RepositoryException, TypeHandlerRegistryException {
        Sail sailStack = new MemoryStore();
        repository = new SailRepository(sailStack);
        repository.initialize();
        TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
        URIResourceTypeHandler uriResourceTypeHandler = new URIResourceTypeHandler();
        StringValueTypeHandler stringValueTypeHandler = new StringValueTypeHandler();
        IntegerValueTypeHandler integerValueTypeHandler = new IntegerValueTypeHandler();
        URLResourceTypeHandler urlResourceTypeHandler = new URLResourceTypeHandler();
        DateValueTypeHandler dateValueTypeHandler = new DateValueTypeHandler();
        LongValueTypeHandler longValueTypeHandler = new LongValueTypeHandler();
        typeHandlerRegistry.registerTypeHandler(uriResourceTypeHandler, java.net.URI.class, XMLSchema.ANYURI);
        typeHandlerRegistry.registerTypeHandler(stringValueTypeHandler, String.class, XMLSchema.STRING);
        typeHandlerRegistry.registerTypeHandler(integerValueTypeHandler, Integer.class, XMLSchema.INTEGER);
        typeHandlerRegistry.registerTypeHandler(integerValueTypeHandler, Integer.class, XMLSchema.INT);
        typeHandlerRegistry.registerTypeHandler(urlResourceTypeHandler, URL.class, XMLSchema.ANYURI);
        typeHandlerRegistry.registerTypeHandler(dateValueTypeHandler, Date.class, XMLSchema.DATE);
        typeHandlerRegistry.registerTypeHandler(longValueTypeHandler, Long.class, XMLSchema.LONG);
        b3 = new DefaultTypedBe3Impl(repository, typeHandlerRegistry);
    }

    @AfterTest
    public void tearDown() {
        b3 = null;
    }

    @Test
    public void testSerialization() throws ParseException, URISyntaxException, RDFizerException {
        b3.serialize(getPerson(), System.out, Format.RDFXML);
    }

    @Test
    public void testGetRDFStatements() throws RDFizerException, URISyntaxException, ParseException {
        List<Statement> statements = b3.getRDFStatements(getPerson());
        Assert.assertNotNull(statements);
        System.out.println(statements.size());
        assertTrue(statements.size() == 20);
        for (Statement statement : statements) {
            System.out.println(statement);
        }
    }

    //TODO: this test fails for the dateTypeHandler
    @Test(enabled = false)
    public void testGetPersonObject() throws URISyntaxException, RDFizerException, ParseException {
        Person person = getPerson();
        List<Statement> statements = b3.getRDFStatements(person);
        Person retrievedPerson = (Person) b3.getObject(
                statements,
                new URIImpl(getIdentifierURI(Person.class).toString() + "/" + person.getId()),
                Person.class
        );
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
        Book book = getBook();
        List<Statement> statements = b3.getRDFStatements(book);
        Comparator<Statement> statementComparator = new StatementComparator();
        Collections.sort(statements, statementComparator);

        Book retrievedBook = b3.getObject(
                statements,
                new URIImpl(getIdentifierURI(Book.class).toString() + "/" + book.getId()),
                Book.class
        );

        assertEquals(statements.size(), 8);
        assertTrue(retrievedBook.getId().equals(book.getId()));
        assertTrue(retrievedBook.getTitle().equals(book.getTitle()));
        assertEqualsNoOrder(retrievedBook.getPages().toArray(), book.getPages().toArray());
    }

    @Test
    public void shouldSerializeSimpleNestedObjects() throws RDFizerException {
        SimpleBook simpleBook = getSimpleBook();
        List<Statement> statements = b3.getRDFStatements(simpleBook);

        Comparator<Statement> statementComparator = new StatementComparator();
        Collections.sort(statements, statementComparator);

//        for (Statement st : statements) {
//            logger.debug("simpleBook statement: " + st);
//        }

        assertEquals(statements.size(), 9);
        //TODO: test more things?
    }

    @Test
    public void shouldAnnotateChildClassAndNotExtendedClass() throws RDFizerException {
        EnhancedResource enhancedResource = getEnhancedResource();
        List<Statement> statements = b3.getRDFStatements(enhancedResource);

        assertTrue(statements.size() == 4);

        EnhancedResource retrievedEnhancedResource = (EnhancedResource) b3.getObject(
                statements,
                new URIImpl(getIdentifierURI(EnhancedResource.class).toString() + "/" + enhancedResource.getId()),
                EnhancedResource.class
        );
        assertNotNull(retrievedEnhancedResource);
        assertTrue(enhancedResource.getId().compareTo(retrievedEnhancedResource.getId()) == 0);
        assertTrue(enhancedResource.getTitle().equals(retrievedEnhancedResource.getTitle()));
        assertEqualsNoOrder(
                enhancedResource.getTopics().toArray(),
                retrievedEnhancedResource.getTopics().toArray()
        );
    }

    @Test
    public void shouldSerializeObjectSkippingNullFields() throws RDFizerException {
        Long pageId = 45L;
        Page pageWithNullContent = Page.PageBuilder.page()
                .withNumber(pageId)
                .havingContent(null)
                .build();
        assertNull(pageWithNullContent.getContent());
        b3.serialize(pageWithNullContent, System.out, Format.RDFXML);
        List<Statement> statements = b3.getRDFStatements(pageWithNullContent);

        Page retrievedPageWithNullField = (Page) b3.getObject(
                statements,
                new URIImpl(getIdentifierURI(Page.class).toString() + "/" + pageWithNullContent.getNumber()),
                Page.class
        );

        assertNull(retrievedPageWithNullField.getContent());
        assertEquals(retrievedPageWithNullField.getNumber(), pageId);
    }

    private EnhancedResource getEnhancedResource() {
        EnhancedResource enhancedResource = new EnhancedResource();
        enhancedResource.setId(new Long(1));
        enhancedResource.setTitle("this is a fake title");
        List<java.net.URI> topics = new ArrayList<java.net.URI>();
        try {
            topics.add(new java.net.URI("http://first.uri/com"));
            topics.add(new java.net.URI("http://second.uri/com"));
        } catch (URISyntaxException e) {
            //should never happen
        }
        enhancedResource.setTopics(topics);
        return enhancedResource;
    }

    public SimpleBook getSimpleBook() {
        SimpleBook simpleBook = simpleBook()
                .withId(1)
                .withMainAuthor(author().withId(1).build())
                .addAuthor(author().withId(2).build())
                .addAuthor(author().withId(3).build())
                .addAuthor(author().withId(4).build())
                .build();
        return simpleBook;
    }

    public Book getBook() {

        Book book = book()
                .withId(new Long(1))
                .withTitle("book title")
                .havingPage(page()
                        .withNumber(new Long(1))
                        .havingContent("first page content")
                        .build())
                .havingPage(page()
                        .withNumber(new Long(2))
                        .havingContent("second page content")
                        .build())
                .build();

        return book;
    }

}