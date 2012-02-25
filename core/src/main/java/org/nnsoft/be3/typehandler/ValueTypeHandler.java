package org.nnsoft.be3.typehandler;

import org.openrdf.model.Value;

public interface ValueTypeHandler<T> extends TypeHandler {

    public Value serialize(T object)
        throws TypeHandlerException;

    public T deserialize(Value value)
        throws TypeHandlerException;

}
