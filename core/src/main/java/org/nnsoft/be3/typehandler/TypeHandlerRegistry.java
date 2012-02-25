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

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;

import java.util.*;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Matteo Moci ( matteo.moci at gmail.com )
 */
public class TypeHandlerRegistry {

    private Map<Class, TypeHandler> classTypeHandlers = new HashMap<Class, TypeHandler>();

    private Map<URI, TypeHandler> dataTypeHandlers = new HashMap<URI, TypeHandler>();

    public void registerTypeHandler(TypeHandler typeHandler, Class clazz, URI datatype)
            throws TypeHandlerRegistryException {
        classTypeHandlers.put(clazz, typeHandler);
        dataTypeHandlers.put(datatype, typeHandler);
    }

    public TypeHandler getTypeHandler(Class clazz) throws TypeHandlerRegistryException {
        if(classTypeHandlers.containsKey(clazz)) {
            return classTypeHandlers.get(clazz);
        }
        throw new TypeHandlerRegistryException("Sorry, but class: '" +
                clazz + "' does not have an associated TypeHandler");
    }

    public TypeHandler getTypeHandler(URI datatype) throws TypeHandlerRegistryException {
        if(dataTypeHandlers.containsKey(datatype)) {
            return dataTypeHandlers.get(datatype);
        }
        throw new TypeHandlerRegistryException("Sorry, but datatype: '" +
                datatype + "' does not have an associated TypeHandler");
    }

    public Set<Class> getHandledTypes() {
        return classTypeHandlers.keySet();
    }

    public Statement getStatement(Resource s, URI p, Object object)
            throws TypeHandlerRegistryException {
        Class clazz = object.getClass();
        TypeHandler typeHandler = classTypeHandlers.get(clazz);
        if (typeHandler instanceof ResourceTypeHandler) {
            ResourceTypeHandler rth = (ResourceTypeHandler) typeHandler;
            try {
                return new StatementImpl(s, p, rth.serialize(object));
            } catch (TypeHandlerException e) {
                throw new TypeHandlerRegistryException("Error while serializing object: '" +
                        object +"'", e);
            }
        }
        if (typeHandler instanceof ValueTypeHandler) {
            ValueTypeHandler vth = (ValueTypeHandler) typeHandler;
            try {
                return new StatementImpl(s, p, vth.serialize(object));
            } catch (TypeHandlerException e) {
                throw new TypeHandlerRegistryException("Error while serializing object: '" +
                        object +"'", e);
            }
        }
        throw new TypeHandlerRegistryException(
                "Cannot find a suitable typehandler for type: '" + object.getClass() + "'");
    }

    public Object getObject(Statement statement)
            throws TypeHandlerRegistryException {
        if(statement.getObject() instanceof Literal) {
            Literal literal = (Literal) statement.getObject();
            ValueTypeHandler vth = (ValueTypeHandler)
                    dataTypeHandlers.get(literal.getDatatype());

            try {
                return vth.deserialize(literal);
            } catch (TypeHandlerException e) {
                throw new TypeHandlerRegistryException("Error while deserializing " + literal.getDatatype().toString(), e);
            }
        }
        if(statement.getObject() instanceof Resource) {
            Resource resource = (Resource) statement.getObject();
            ResourceTypeHandler rth = (ResourceTypeHandler)
                    classTypeHandlers.get(java.net.URI.class);
            try {
                return rth.deserialize(resource);
            } catch (TypeHandlerException e) {
                throw new TypeHandlerRegistryException("Error while deserializing", e);
            }
        }
        throw new TypeHandlerRegistryException("Sorry, but seems that object of statement: '" +
                statement + "' is nor a Resource or a Literal");
    }

}
