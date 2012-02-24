package com.collective.rdfizer;

/**
 * Raised if something goes wrong within {@link com.collective.rdfizer.RDFizer}.
 * 
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RDFizerException extends Exception {

    public RDFizerException(String message) {
        super(message);
    }

    public RDFizerException(String message, Exception e) {
        super(message, e);
    }
}
