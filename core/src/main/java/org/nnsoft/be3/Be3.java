package org.nnsoft.be3;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import java.io.OutputStream;
import java.util.List;

/**
 * Defines the main contract of a class able to serialize <i>Java</i> beans into
 * <i>RDF</i>.
 *
 * @see {@link org.nnsoft.be3.annotations}
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Be3 {
    
    public void serialize(Object object, OutputStream outputStream, Format format)
            throws RDFizerException;

    public List<Statement> getRDFStatements(Object object)
        throws RDFizerException;

    public Object getObject(List<Statement> statements, URI identifier, Class clazz)
        throws RDFizerException;

}
