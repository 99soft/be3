package com.collective.rdfizer.typehandler;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LongValueTypeHandler implements ValueTypeHandler<Long> {

    public Value serialize(Long object) throws TypeHandlerException {
        return new LiteralImpl(object.toString(), XMLSchema.LONG);
    }

    public Long deserialize(Value resource) throws TypeHandlerException {
        try {
        return new Long(resource.stringValue());
        } catch(NumberFormatException e) {
            throw new TypeHandlerException("The value: '" 
                    + resource.stringValue() + "' cannot be represented with a long");
        }
    }

    public Class getType() {
        return Long.class;
    }
}
