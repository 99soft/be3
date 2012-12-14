package org.nnsoft.be3.model.namespace;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.annotations.RDFIdentifier;
import org.nnsoft.be3.annotations.RDFNamespace;
import org.nnsoft.be3.annotations.RDFProperty;

import java.util.UUID;

/**
 * @author Matteo Moci ( matteo (dot) moci (at) gmail (dot) com )
 */

@RDFNamespace(prefix = "http://mynamespace.com")
@RDFClassType(type = "http://example.com/Polarity")
public class Polarity {

    private UUID id;

    private String name;

    private static final String NAMESPACE = "";

    public Polarity() {}

    public Polarity(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

    @RDFIdentifier
    public UUID getId() {
        return id;
    }

    public void setId(String id) {
        this.id = UUID.fromString(id);
    }

    @RDFProperty(properties = {"http://namespace.com/hasName"})
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Polarity polarity = (Polarity) o;

        if (name != null ? !name.equals(polarity.name) : polarity.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Polarity{" +
                "name='" + name + '\'' +
                '}';
    }
}
