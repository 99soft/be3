package org.nnsoft.be3.typehandler;

import java.net.URL;

import org.nnsoft.be3.model.MyURL;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;

public class MyURLResourceTypeHandler implements TypeHandler {
    
    public Resource serialize(final URL object) throws TypeHandlerException {
    
        return new URIImpl(object.toString());
    }
    
    public MyURL deserialize(final Resource resource) throws TypeHandlerException {
    
        return new MyURL(resource.stringValue());
        
    }
    
    @Override
    public Class getType() {
    
        return MyURL.class;
    }
}
