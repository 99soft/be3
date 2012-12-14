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

    public <T> T getObject(List<Statement> statements, URI identifier, Class<T> clazz)
        throws RDFizerException;

    public URI getIdentifierPrefix(Class clazz) throws RDFizerException;
}
