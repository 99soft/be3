package com.collective.rdfizer.model.nested;

import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.annotations.RDFIdentifier;
import com.collective.rdfizer.annotations.RDFProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */

@RDFClassType(type = "http://collective.com/SimpleBook")
public class SimpleBook {

    private int id;
    private Author mainAuthor;
    private List<Author> authors = new ArrayList<Author>();

    @RDFIdentifier
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @RDFProperty (properties = {"http://collective.com/SimpleBook#mainAuthor"})
    public Author getMainAuthor() {
        return mainAuthor;
    }

    public void setMainAuthor(Author mainAuthor) {
        this.mainAuthor = mainAuthor;
    }

    public SimpleBook(int id) {
        this.id = id;
    }

    @RDFProperty (properties = {"http://collective.com/SimpleBook#author"})
    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public void addAuthor(Author author) {
        this.authors.add(author);
    }
}
