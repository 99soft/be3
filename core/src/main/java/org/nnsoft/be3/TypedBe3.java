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

import org.nnsoft.be3.typehandler.TypeHandler;
import org.nnsoft.be3.typehandler.TypeHandlerRegistry;
import org.nnsoft.be3.typehandler.TypeHandlerRegistryException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
public abstract class TypedBe3 implements Be3 {

    protected Repository repository;

    protected TypeHandlerRegistry typeHandlerRegistry;

    public TypedBe3(Repository repository, TypeHandlerRegistry typeHandlerRegistry) {
        this.repository = repository;
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    public void registerTypeHandler(final TypeHandler typeHandler, final Class clazz,
                                    final URI datatype) throws RDFizerException {

        try {
            this.typeHandlerRegistry.registerTypeHandler(typeHandler, clazz, datatype);
        } catch (TypeHandlerRegistryException e) {
            throw new RDFizerException("failed registering typeHandler with params: '" +
                                       typeHandler.getType().getName().toString() + " " +
                                       clazz.getName().toString() +
                                       " " + datatype.toString());
        }
    }

}
