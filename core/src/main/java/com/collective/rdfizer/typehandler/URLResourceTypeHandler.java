package com.collective.rdfizer.typehandler;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class URLResourceTypeHandler implements ResourceTypeHandler<URL> {

    public Resource serialize(URL object) throws TypeHandlerException {
        return new URIImpl(object.toString());
    }

    public URL deserialize(Resource resource) throws TypeHandlerException {
        try {
            return new URL(resource.stringValue());
        } catch (MalformedURLException e) {
            throw new TypeHandlerException("The provided resources cannot be represented as a URL", e);
        }
    }

    public Class getType() {
        return URL.class;
    }
}
