package org.nnsoft.be3.typehandler;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TypeHandlerRegistryException extends Exception {

    public TypeHandlerRegistryException(String message) {
        super(message);
    }

    public TypeHandlerRegistryException(String message, Exception e) {
        super(message, e);
    }

}
