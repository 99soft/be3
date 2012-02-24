package com.collective.rdfizer.typehandler;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TypeHandlerRegistryException extends Exception {

    public TypeHandlerRegistryException(String s, TypeHandlerException e) {
        super(s, e);
    }

    public TypeHandlerRegistryException(String s) {
        super(s);
    }
}
