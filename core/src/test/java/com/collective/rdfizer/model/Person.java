package com.collective.rdfizer.model;

import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.annotations.RDFIdentifier;
import com.collective.rdfizer.annotations.RDFProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Mock up class just for test purposes.
 * 
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@RDFClassType(type = "http://collective.com/profile/Person")
public class Person {

    private UUID id;

    private String name;

    private String surname;

    private Date birthDate;

    private List<String> tags = new ArrayList<String>();

    private List<Person> knows = new ArrayList<Person>();

    private List<URI> concepts = new ArrayList<URI>();

    public Person() {}

    public Person(String name, String surname) {
        id = UUID.randomUUID();
        this.name = name;
        this.surname = surname;
    }
    
    @RDFIdentifier
    public UUID getId() {
        return id;
    }

    public void setId(String id) {
        this.id = UUID.fromString(id); 
    }

    @RDFProperty(
            properties = {
                    "http://xmlns.org/01/foaf/firstName", 
                    "http://collective.com/profile/name"}
    )
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @RDFProperty(
            properties = {
                    "http://xmlns.org/01/foaf/secondName",
                    "http://collective.com/profile/surname"
            }
    )    
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @RDFProperty(properties = { "http://xmlns.org/01/foaf/birthDate" })
    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date date) {
        this.birthDate = date;
    }

    @RDFProperty(properties = {"http://collective.com/profile/skill"})
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean addTag(String tag) {
        return this.tags.add(tag);
    }

    @RDFProperty(properties = {"http://xmlns.org/01/foaf/knows"})
    public List<Person> getKnows() {
        return knows;
    }

    public void setKnows(List<Person> knows) {
        this.knows = knows;
    }

    public boolean addKnow(Person person) {
        return this.knows.add(person);
    }

    @RDFProperty( properties = { "http://collective.com/profiles/concept" } )
    public List<URI> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<URI> concepts) {
        this.concepts = concepts;
    }

    public boolean addConcept(URI concept) {
        return this.concepts.add(concept);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (id != null ? !id.equals(person.id) : person.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", tags=" + tags +
                ", knows='" + knows + '\'' +
                ", concepts='" + concepts +
                '}';
    }
}
