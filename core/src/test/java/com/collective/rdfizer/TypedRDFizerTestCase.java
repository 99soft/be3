package com.collective.rdfizer;

import com.collective.model.ProjectInvolvement;
import com.collective.model.persistence.SourceRss;
import com.collective.model.persistence.WebResource;
import com.collective.model.persistence.enhanced.SourceRssEnhanced;
import com.collective.model.persistence.enhanced.WebResourceEnhanced;
import com.collective.model.profile.ProjectProfile;
import com.collective.model.profile.UserProfile;
import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.model.hierarchy.EnhancedResource;
import com.collective.rdfizer.model.nested.Author;
import com.collective.rdfizer.model.nested.Book;
import com.collective.rdfizer.model.nested.Page;
import com.collective.rdfizer.model.Person;
import com.collective.rdfizer.model.nested.SimpleBook;
import com.collective.rdfizer.typehandler.*;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import virtuoso.sesame2.driver.VirtuosoRepository;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * Reference test class for {@link com.collective.rdfizer.TypedRDFizer}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
public class TypedRDFizerTestCase {

    private RDFizer rdFizer;

    private Repository repository;

    private static Logger logger = Logger.getLogger(TypedRDFizerTestCase.class);

    private static final String VIRTUOSO_CONNECTION = "jdbc:virtuoso://cibionte.cybion.eu:1111";

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
        rdFizer = new TypedRDFizer(repository, typeHandlerRegistry);
    }

    @AfterTest
    public void tearDown() {
        rdFizer = null;
    }

    @Test
    public void testSerialization() throws ParseException, URISyntaxException, RDFizerException {
        rdFizer.serialize(getPerson(), System.out, Format.RDFXML);
    }

    @Test
    public void testGetRDFStatements() throws RDFizerException, URISyntaxException, ParseException {
        List<Statement> statements = rdFizer.getRDFStatements(getPerson());
        Assert.assertNotNull(statements);
        System.out.println(statements.size());
        Assert.assertTrue(statements.size() == 20);
        for (Statement statement : statements) {
            System.out.println(statement);
        }
    }


    //TODO: this test fails for the dateTypeHandler
    @Test(enabled = false)
    public void testGetPersonObject() throws URISyntaxException, RDFizerException, ParseException {
        Person person = getPerson();
        List<Statement> statements = rdFizer.getRDFStatements(person);
        Person retrievedPerson = (Person) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(Person.class).toString() + "/" + person.getId()),
                Person.class
        );
        Assert.assertNotNull(retrievedPerson);
        Assert.assertEquals(person, retrievedPerson);
        Assert.assertEquals(person.getName(), retrievedPerson.getName());
        Assert.assertEquals(person.getSurname(), retrievedPerson.getSurname());
        Assert.assertEquals(person.getBirthDate().toString(), retrievedPerson.getBirthDate().toString());
        Assert.assertEqualsNoOrder(person.getConcepts().toArray(), retrievedPerson.getConcepts().toArray());
        Assert.assertEqualsNoOrder(person.getKnows().toArray(), retrievedPerson.getKnows().toArray());
        Assert.assertEqualsNoOrder(person.getTags().toArray(), retrievedPerson.getTags().toArray());

    }

    @Test
    public void testGetProfileObject() throws URISyntaxException, RDFizerException, ParseException {
        UserProfile profile = new UserProfile();
        profile.setId(new Long(73662));
        profile.addInterest(new java.net.URI("http://dbpedia.org/resource/Basketball"));
        profile.addInterest(new java.net.URI("http://dbpedia.org/resource/Travelling"));
        profile.addSkill(new java.net.URI("http://dbpedia.org/resource/Maven"));
        profile.addSkill(new java.net.URI("http://dbpedia.org/resource/Java"));
        List<Statement> statements = rdFizer.getRDFStatements(profile);
        UserProfile retrievedProfile = (UserProfile) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(UserProfile.class).toString() + "/" + profile.getId()),
                UserProfile.class
        );
        Assert.assertNotNull(retrievedProfile);
        Assert.assertEquals(profile, retrievedProfile);
        Assert.assertEqualsNoOrder(profile.getInterests().toArray(), retrievedProfile.getInterests().toArray());
        Assert.assertEqualsNoOrder(profile.getSkills().toArray(), retrievedProfile.getSkills().toArray());
    }

    @Test
    public void testGetWebResourceEnhancedObject()
            throws MalformedURLException, URISyntaxException, RDFizerException {
        WebResourceEnhanced resource = getEnhancedWebResource();

        resource.setId(new Integer(5648));
        resource.setTitolo("Just a fake resource");
        resource.setDescrizione("This fake web resource it's only for testing purposes");
        resource.setUrl(new URL("http://davidepalmisano.com/fake-page.html"));
        resource.addTopic(new java.net.URI("http://dbpedia.org/resource/Semantic_Web"));
        resource.addTopic(new java.net.URI("http://dbpedia.org/resource/Web_of_data"));
        resource.addTopic(new java.net.URI("http://dbpedia.org/resource/Java"));
        List<Statement> statements = rdFizer.getRDFStatements(resource);

        WebResourceEnhanced retrievedResource = (WebResourceEnhanced) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(WebResourceEnhanced.class).toString() + "/" + resource.getId()),
                WebResourceEnhanced.class
        );
        Assert.assertNotNull(retrievedResource);
        Assert.assertEquals(resource, retrievedResource);
        Assert.assertEqualsNoOrder(resource.getTopics().toArray(), retrievedResource.getTopics().toArray());
        Assert.assertEquals(resource.getDescrizione(), retrievedResource.getDescrizione());
        Assert.assertEquals(resource.getTitolo(), retrievedResource.getTitolo());
        Assert.assertEquals(resource.getUrl(), retrievedResource.getUrl());
    }

    @Test
    public void testGetProjectProfile() throws URISyntaxException, RDFizerException {
        ProjectProfile projectProfile = new ProjectProfile();
        projectProfile.setId(new Long(24636));
        projectProfile.addManifestoConcept(new java.net.URI("http://dbpedia.org/resource/Example"));
        projectProfile.addManifestoConcept(new java.net.URI("http://dbpedia.org/resource/Fake"));
        List<Statement> statements = rdFizer.getRDFStatements(projectProfile);
        Assert.assertTrue(statements.size() == 3);
        ProjectProfile retrievedProjectProfile = (ProjectProfile) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(ProjectProfile.class).toString() + "/" + projectProfile.getId()),
                ProjectProfile.class
        );
        Assert.assertNotNull(retrievedProjectProfile);
        Assert.assertEqualsNoOrder(
                projectProfile.getManifestoConcepts().toArray(),
                retrievedProjectProfile.getManifestoConcepts().toArray()
        );
    }

    /**
     * This test checks the behavior of the {@link com.collective.rdfizer.TypedRDFizer}
     * when tries to deserialize triples with no datatypes specified.
     */
    // TODO make this atomic putting a test dependancy to Jena Beans
    @Test(enabled = false)
    public void testGetExistentUser() throws RepositoryException, RDFizerException {
        final URI identifier =
                new URIImpl("http://xmlns.com/foaf/0.1/Person/00000000-0000-0001-0000-000000000005");
        Repository repository = new VirtuosoRepository(
                VIRTUOSO_CONNECTION,
                "dba",
                "cybiondba"
        );
        repository.initialize();
        // getting a profile that we presume it's already there
        RepositoryConnection repositoryConnection = repository.getConnection();
        RepositoryResult reposititoryResult = repositoryConnection.getStatements(
                identifier,
                null,
                null, 
                false
        );
        List<Statement> statements = reposititoryResult.asList();
        UserProfile expected = (UserProfile) rdFizer.getObject(statements, identifier, UserProfile.class);
        Assert.assertNotNull(expected);
    }

    @Test
    public void shouldDeserializeExistingWebResourceEnhanced()
            throws RepositoryException, RDFizerException, QueryEvaluationException, MalformedQueryException {
        final URI identifier =
                new URIImpl("http://collective.com/resources/web/28815");
        Repository repository = new VirtuosoRepository(
                VIRTUOSO_CONNECTION,
                "dba",
                "cybiondba"
        );
        repository.initialize();
        RepositoryConnection repositoryConnection = repository.getConnection();
        String queryString = "CONSTRUCT\n" +
                "{\n" +
                "<" + identifier.toString() + "> ?p ?o.\n" +
                "?q ?r ?t.\n" +
                "}\n" +
                "FROM <http://collective.com/resources/web/alternative>\n" +
                "\n" +
                "WHERE {\n" +
                "<" + identifier.toString() + "> ?p ?o.\n" +
                "<" + identifier.toString() + "> <http://collective.com/resources/web/hasSourceRSS> ?q.\n" +
                "?q ?r ?t.\n" +
                "}";
        GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
        GraphQueryResult result = graphQuery.evaluate();
        logger.debug("stop here");
        List<Statement> stmts = new ArrayList<Statement>();
        while (result.hasNext()) {
            stmts.add(result.next());
        }
        WebResourceEnhanced webResourceEnhanced = (WebResourceEnhanced)
                rdFizer.getObject(stmts, identifier, WebResourceEnhanced.class);
        logger.debug(webResourceEnhanced);

        result.close();
        repositoryConnection.close();
    }

    @Test
    public void shouldSerializeNestedObjects() throws RDFizerException {
        Book book = getBook();
        List<Statement> statements = rdFizer.getRDFStatements(book);
        Comparator<Statement> statementComparator = new StatementComparator();
        Collections.sort(statements, statementComparator);

        Book retrievedBook = (Book) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(Book.class).toString() + "/" + book.getId()),
                Book.class
        );

        Assert.assertEquals(statements.size(), 8);
        Assert.assertTrue(retrievedBook.getId().equals(book.getId()));
        Assert.assertTrue(retrievedBook.getTitle().equals(book.getTitle()));
        Assert.assertEqualsNoOrder(retrievedBook.getPages().toArray(), book.getPages().toArray());
    }

    @Test
    public void shouldSerializeSimpleNestedObjects() throws RDFizerException {
        SimpleBook simpleBook = getSimpleBook();
        List<Statement> statements = rdFizer.getRDFStatements(simpleBook);

        Comparator<Statement> statementComparator = new StatementComparator();
        Collections.sort(statements, statementComparator);

//        for (Statement st : statements) {
//            logger.debug("simpleBook statement: " + st);
//        }

        Assert.assertEquals(statements.size(), 7);
        //TODO: test more things?
    }

    @Test
    public void shouldAnnotateChildClassAndNotExtendedClass() throws RDFizerException {
        EnhancedResource enhancedResource = getEnhancedResource();
        List<Statement> statements = rdFizer.getRDFStatements(enhancedResource);

        Assert.assertTrue(statements.size() == 4);

        EnhancedResource retrievedEnhancedResource = (EnhancedResource) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(EnhancedResource.class).toString() + "/" + enhancedResource.getId()),
                EnhancedResource.class
        );
        Assert.assertNotNull(retrievedEnhancedResource);
        Assert.assertTrue(enhancedResource.getId().compareTo(retrievedEnhancedResource.getId()) == 0);
        Assert.assertTrue(enhancedResource.getTitle().equals(retrievedEnhancedResource.getTitle()));
        Assert.assertEqualsNoOrder(
                enhancedResource.getTopics().toArray(),
                retrievedEnhancedResource.getTopics().toArray()
        );
    }

    //already existed
    @Test(enabled = true)
    public void shouldSerializeWebResourceEnhanced() throws MalformedURLException, RDFizerException {

        WebResourceEnhanced webResourceEnhanced = getEnhancedWebResource();

        List<Statement> statements = rdFizer.getRDFStatements(webResourceEnhanced);

        for (Statement st : statements) {
            logger.debug("statement: " + st);
        }

        Assert.assertTrue(statements.size() == 9);

        WebResourceEnhanced retrievedWebResourceEnhanced = (WebResourceEnhanced) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(WebResourceEnhanced.class).toString() + "/" + webResourceEnhanced.getId()),
                WebResourceEnhanced.class
        );
        Assert.assertNotNull(retrievedWebResourceEnhanced);
        Assert.assertTrue(webResourceEnhanced.getId().compareTo(retrievedWebResourceEnhanced.getId()) == 0);
        Assert.assertTrue(webResourceEnhanced.getTitolo().equals(retrievedWebResourceEnhanced.getTitolo()));
        Assert.assertEqualsNoOrder(
                webResourceEnhanced.getTopics().toArray(),
                retrievedWebResourceEnhanced.getTopics().toArray()
        );
    }

    @Test
    public void shouldSerializeDeserializeSourceRssEnhanced() throws RDFizerException,
            MalformedURLException {
        SourceRssEnhanced sourceRssEnhanced = getSourceRssEnhanced();

        List<Statement> statements = rdFizer.getRDFStatements(sourceRssEnhanced);

        SourceRssEnhanced retrievedObject = (SourceRssEnhanced) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(SourceRssEnhanced.class).toString() + "/" + sourceRssEnhanced.getId()),
                SourceRssEnhanced.class
        );

        Assert.assertNotNull(retrievedObject);
        logger.debug("sourceRssEnhanced: " + retrievedObject);
    }

    @Test
    public void shouldSerializeProjectInvolvement() throws URISyntaxException, RDFizerException {

        //project profile
        ProjectProfile projectProfile = new ProjectProfile();
        projectProfile.setId(new Long(99));
        List<java.net.URI> manifestoConcepts = new ArrayList<java.net.URI>();
        manifestoConcepts.add(new java.net.URI("http://first.com"));
        manifestoConcepts.add(new java.net.URI("http://second.com"));
        projectProfile.setManifestoConcepts(manifestoConcepts);

        //involvement
        ProjectInvolvement projectInvolvement1 = new ProjectInvolvement();

        projectInvolvement1.setUserProfile(new java.net.URI("http://user.profile.com/1"));
        projectInvolvement1.setRole("fake role");
        projectInvolvement1.setSince(new DateTime());
        projectInvolvement1.setTo(new DateTime().plusDays(7));
        //strategy for the id to be unique
        String idProjectInvolvement = projectProfile.getId().toString()
                .concat(String.valueOf(projectInvolvement1.getRole().hashCode()));

        projectInvolvement1.setId(Long.parseLong(idProjectInvolvement));

        List<ProjectInvolvement> projectInvolvements = new ArrayList<ProjectInvolvement>();
        projectInvolvements.add(projectInvolvement1);
        //to test, comment it out and see that rdfizer serializes correctly the partial object
        //set project involvements
        projectProfile.setProjectInvolvements(projectInvolvements);

        List<Statement> statements = rdFizer.getRDFStatements(projectProfile);
        for (Statement s : statements) {
            logger.debug("stmt: " + s.toString());
        }

        ProjectProfile retrievedObject = (ProjectProfile) rdFizer.getObject(
                statements,
                new URIImpl(getIdentifierURI(ProjectProfile.class).toString() + "/" + projectProfile.getId()),
                ProjectProfile.class);

        Assert.assertNotNull(retrievedObject);
        Assert.assertEquals(retrievedObject.getProjectInvolvements().get(0), projectInvolvement1);
        Assert.assertEquals(statements.size(), 7);
    }

    private UserProfile getUserProfile(int i) throws URISyntaxException {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(new Long(i));
        return userProfile;
    }

    private SourceRssEnhanced getSourceRssEnhanced() throws MalformedURLException {
        SourceRssEnhanced sourceRssEnhanced = new SourceRssEnhanced();
        sourceRssEnhanced.setId(1);
        sourceRssEnhanced.setCategoria("categoria");
        sourceRssEnhanced.setUrl(new URL("http://fakeurl.com"));
        return sourceRssEnhanced;
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
        SimpleBook simpleBook = new SimpleBook(1);
        Author author = new Author(2);
        Author author3 = new Author(3);
        Author author4 = new Author(4);

        simpleBook.setMainAuthor(author);
        simpleBook.addAuthor(author3);
        simpleBook.addAuthor(author4);
        return simpleBook;
    }

    public Book getBook() {
        Page first = new Page();
        first.setNumber(new Long(1));
        first.setContent("first page content");

        Page second = new Page();
        second.setNumber(new Long(2));
        second.setContent("second page content");

        Book book = new Book();
        book.setId(new Long(1));
        book.setTitle("book title");
        book.addPage(first);
        book.addPage(second);

        return book;
    }

    private WebResourceEnhanced getEnhancedWebResource() throws MalformedURLException {
        WebResource webResource = new WebResource();
        webResource.setId(new Integer(4));
        webResource.setTitolo("fake title");
        webResource.setDescrizione("fake description");
        webResource.setUrl(new URL("http://www.fakeurl.com"));


        SourceRss sourceRss = new SourceRss();
        sourceRss.setId(new Integer(1));
        sourceRss.setCategoria("fake category");
        sourceRss.setUrl(new URL("http://fakeurlsourcerss.com"));

        webResource.setSourceRss(sourceRss);

        List<java.net.URI> concepts = new ArrayList<java.net.URI>();
        try {
            concepts.add(new java.net.URI("http://first.com"));
        } catch (URISyntaxException e) {
            //should never happen
        }
        WebResourceEnhanced enh = new WebResourceEnhanced(webResource, concepts);
        return enh;
    }

}