package org.nnsoft.be3.typehandler;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TypeHandlerException extends Exception {

    public TypeHandlerException(String s) {
        super(s);
    }

    public TypeHandlerException(String message, Exception e) {
        super(message, e);
    }

}
