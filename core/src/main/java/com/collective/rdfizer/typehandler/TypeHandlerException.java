package com.collective.rdfizer.typehandler;

import java.net.URISyntaxException;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TypeHandlerException extends Exception {
    
    public TypeHandlerException(String message, Exception e) {
        super(message, e);
    }

    public TypeHandlerException(String s) {
        super(s);
    }
}
