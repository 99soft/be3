package com.collective.rdfizer.typehandler;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class IntegerValueTypeHandler implements ValueTypeHandler<Integer> {

    public Value serialize(Integer object) throws TypeHandlerException {
        return new LiteralImpl(object.toString(), XMLSchema.INTEGER);
    }

    public Integer deserialize(Value resource) throws TypeHandlerException {
        try {
        return new Integer(resource.stringValue());
        } catch(NumberFormatException e) {
            throw new TypeHandlerException("The value: '"
                    + resource.stringValue() + "' cannot be represented with an integer");
        }
    }

    public Class getType() {
        return Integer.class;
    }
}
