package com.collective.rdfizer.typehandler;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class URIResourceTypeHandler implements ResourceTypeHandler<URI> {

    public Resource serialize(URI object) throws TypeHandlerException {
        return new URIImpl(object.toString());
    }

    public URI deserialize(Resource resource) throws TypeHandlerException {
        try {
            return new URI(resource.stringValue());
        } catch (URISyntaxException e) {
            throw new TypeHandlerException("the provided resource is not representable with a URI", e);
        }
    }

    public Class getType() {
        return URI.class;
    }
}
