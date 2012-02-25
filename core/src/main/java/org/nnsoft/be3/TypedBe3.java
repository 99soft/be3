package org.nnsoft.be3;

import org.nnsoft.be3.typehandler.TypeHandlerRegistry;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;

import java.io.OutputStream;
import java.util.List;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class TypedBe3 implements Be3 {

    protected Repository repository;

    protected TypeHandlerRegistry typeHandlerRegistry;

    public TypedBe3(Repository repository, TypeHandlerRegistry typeHandlerRegistry) {
        this.repository = repository;
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    abstract public void serialize(Object object, OutputStream outputStream, Format format)
            throws RDFizerException;

    abstract public Object getObject(List<Statement> statements, URI identifier, Class clazz)
        throws RDFizerException;

}
