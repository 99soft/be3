package com.collective.rdfizer.typehandler;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DateValueTypeHandler implements ValueTypeHandler<Date> {

    //TODO: TESTS FAIL when parsing date formats with this expression
    private SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");

    public Value serialize(Date object) throws TypeHandlerException {
        return new LiteralImpl(object.toString(), XMLSchema.DATE);
    }

    public Date deserialize(Value resource) throws TypeHandlerException {
        try {
            return simpleDateFormat.parse(resource.stringValue());
        } catch (ParseException e) {
            throw new TypeHandlerException("Error while parsing date:'" +
                    resource.stringValue()  + "'", e);
        }
    }

    public Class getType() {
        return Date.class;
    }
}
