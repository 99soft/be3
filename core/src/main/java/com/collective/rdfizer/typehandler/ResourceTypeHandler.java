package com.collective.rdfizer.typehandler;

import org.openrdf.model.Resource;


public interface ResourceTypeHandler<T> extends TypeHandler {

    public Resource serialize(T object)
        throws TypeHandlerException;

    public T deserialize(Resource resource)
        throws TypeHandlerException;

}
