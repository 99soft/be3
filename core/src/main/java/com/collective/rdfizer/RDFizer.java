package com.collective.rdfizer;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import java.io.OutputStream;
import java.util.List;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface RDFizer {
    
    public void serialize(Object object, OutputStream outputStream, Format format)
            throws RDFizerException;

    public List<Statement> getRDFStatements(Object object)
        throws RDFizerException;

    public Object getObject(List<Statement> statements, URI identifier, Class clazz)
        throws RDFizerException;

}
