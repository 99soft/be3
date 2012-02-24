package com.collective.rdfizer.typehandler;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class StringValueTypeHandler implements ValueTypeHandler<String> {

    public Value serialize(String object) throws TypeHandlerException {
        return new LiteralImpl(object, XMLSchema.STRING);
    }

    public String deserialize(Value resource) throws TypeHandlerException {
        return resource.stringValue();
    }

    public Class getType() {
        return String.class;
    }
}
