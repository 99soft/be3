package org.nnsoft.be3.model.nested;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.annotations.RDFIdentifier;

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
