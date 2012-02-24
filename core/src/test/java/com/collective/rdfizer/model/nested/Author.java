package com.collective.rdfizer.model.nested;

import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.annotations.RDFIdentifier;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
@RDFClassType(type = "http://collective.com/Author")
public class Author {

    private int id;

    @RDFIdentifier
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Author(int id) {
        this.id = id;
    }
}
