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
package org.nnsoft.be3.typehandler;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * @author micheleminno
 */
public class DoubleValueTypeHandler implements ValueTypeHandler<Double> {

	public Value serialize(final Double object) throws TypeHandlerException {
		return new LiteralImpl(object.toString(), XMLSchema.DOUBLE);
	}

	public Double deserialize(final Value resource) throws TypeHandlerException {
		try {
			return new Double(resource.stringValue());
		} catch (final NumberFormatException e) {
			throw new TypeHandlerException("The value: '"
					+ resource.stringValue()
					+ "' cannot be represented with a double");
		}
	}

	public Class getType() {
		return Double.class;
	}
}
