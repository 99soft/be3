/*
 * Copyright (c) 2012 The 99 Software Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.nnsoft.be3;

import java.net.URL;
import java.util.Date;

import org.nnsoft.be3.typehandler.DateValueTypeHandler;
import org.nnsoft.be3.typehandler.DoubleValueTypeHandler;
import org.nnsoft.be3.typehandler.IntegerValueTypeHandler;
import org.nnsoft.be3.typehandler.LongValueTypeHandler;
import org.nnsoft.be3.typehandler.StringValueTypeHandler;
import org.nnsoft.be3.typehandler.TypeHandler;
import org.nnsoft.be3.typehandler.TypeHandlerRegistry;
import org.nnsoft.be3.typehandler.TypeHandlerRegistryException;
import org.nnsoft.be3.typehandler.URIResourceTypeHandler;
import org.nnsoft.be3.typehandler.URLResourceTypeHandler;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
public abstract class TypedBe3 implements Be3 {
    
    protected Repository repository;
    
    protected TypeHandlerRegistry typeHandlerRegistry;
    
    public TypedBe3(final Repository repository, final TypeHandlerRegistry typeHandlerRegistry) {
    
        this.repository = repository;
        this.typeHandlerRegistry = typeHandlerRegistry;
    }
    
    public TypedBe3(final Repository repository) {
    
        this(repository, new TypeHandlerRegistry());
        initialiseRegistry();
    }
    
    public TypedBe3() {

        this(new SailRepository(new MemoryStore()));
        initialiseRepository();
    }
    
    private void initialiseRepository() {
    
        try {
            repository.initialize();
        } catch (final RepositoryException e) {
            throw new RuntimeException("Failed to init Sail repository", e);
        }
    }
    
    private void initialiseRegistry() {
    
        final URIResourceTypeHandler uriResourceTypeHandler = new URIResourceTypeHandler();
        final StringValueTypeHandler stringValueTypeHandler = new StringValueTypeHandler();
        final IntegerValueTypeHandler integerValueTypeHandler = new IntegerValueTypeHandler();
        final URLResourceTypeHandler urlResourceTypeHandler = new URLResourceTypeHandler();
        final DateValueTypeHandler dateValueTypeHandler = new DateValueTypeHandler();
        final LongValueTypeHandler longValueTypeHandler = new LongValueTypeHandler();
        final DoubleValueTypeHandler doubleValueTypeHandler = new DoubleValueTypeHandler();
        
        try {
            typeHandlerRegistry.registerTypeHandler(uriResourceTypeHandler, java.net.URI.class,
                    XMLSchema.ANYURI);
            typeHandlerRegistry.registerTypeHandler(stringValueTypeHandler, String.class,
                    XMLSchema.STRING);
            typeHandlerRegistry.registerTypeHandler(integerValueTypeHandler, Integer.class,
                    XMLSchema.INTEGER);
            typeHandlerRegistry.registerTypeHandler(integerValueTypeHandler, Integer.class,
                    XMLSchema.INT);
            typeHandlerRegistry.registerTypeHandler(urlResourceTypeHandler, URL.class,
                    XMLSchema.ANYURI);
            typeHandlerRegistry.registerTypeHandler(dateValueTypeHandler, Date.class,
                    XMLSchema.DATE);
            typeHandlerRegistry.registerTypeHandler(longValueTypeHandler, Long.class,
                    XMLSchema.LONG);
            typeHandlerRegistry.registerTypeHandler(doubleValueTypeHandler, Double.class,
                    XMLSchema.DOUBLE);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to register type handlers", e);
        }
    }
    
    public void registerTypeHandler(final TypeHandler typeHandler, final Class clazz,
            final URI datatype) throws RDFizerException {
    
        try {
            this.typeHandlerRegistry.registerTypeHandler(typeHandler, clazz, datatype);
        } catch (final TypeHandlerRegistryException e) {
            throw new RDFizerException("failed registering typeHandler with params: '"
                    + typeHandler.getType().getName().toString() + " " + clazz.getName().toString()
                    + " " + datatype.toString());
        }
    }
    
}
