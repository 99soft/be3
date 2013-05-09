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

import static java.lang.String.format;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.annotations.RDFIdentifier;
import org.nnsoft.be3.annotations.RDFNamespace;
import org.nnsoft.be3.annotations.RDFProperty;
import org.nnsoft.be3.typehandler.TypeHandlerException;
import org.nnsoft.be3.typehandler.TypeHandlerRegistry;
import org.nnsoft.be3.typehandler.TypeHandlerRegistryException;
import org.nnsoft.be3.typehandler.ValueTypeHandler;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class DefaultTypedBe3Impl extends TypedBe3 {
    
    private final Logger LOGGER = LoggerFactory.getLogger(DefaultTypedBe3Impl.class);
    
    public DefaultTypedBe3Impl(final Repository repository,
            final TypeHandlerRegistry typeHandlerRegistry) {
    
        super(repository, typeHandlerRegistry);
    }
    
    public DefaultTypedBe3Impl(final Repository repository) {
    
        super(repository);
    }
    
    public DefaultTypedBe3Impl() {
    
        super();
    }
    
    @Override
    public void serialize(final Object object, final OutputStream outputStream, final Format format)
            throws RDFizerException {
    
        final List<Statement> statements = getRDFStatements(object);
        try {
            super.repository.getConnection().add(statements);
            super.repository.getConnection().commit();
        } catch (final RepositoryException e) {
            throw new RDFizerException("Error while adding RDF Statements of object '" + object
                    + "'", e);
        }
        try {
            super.repository.getConnection().export(new RDFXMLPrettyWriter(outputStream));
            super.repository.getConnection().clear((Resource) null);
            super.repository.getConnection().commit();
            super.repository.getConnection().close();
        } catch (final RepositoryException e) {
            throw new RDFizerException("Error while producing RDF/XML serialization", e);
        } catch (final RDFHandlerException e) {
            throw new RDFizerException("Error while producing RDF/XML serialization", e);
        }
    }
    
    @Override
    public List<Statement> getRDFStatements(final Object object) throws RDFizerException {
    
        return getStatements(object, new HashSet<Statement>());
    }
    
    @Override
    public <T> T getObject(final List<Statement> statements, final URI identifier,
            final Class<T> clazz) throws RDFizerException {
    
        if (clazz.equals(java.net.URI.class)) {
            try {
                return (T) new java.net.URI(identifier.toString());
            } catch (final URISyntaxException e) {
                throw new RDFizerException(
                        "Error: provided identifier '%s' is not a well-formed URI", identifier);
            }
        }
        if (clazz.equals(java.net.URL.class)) {
            try {
                return (T) new java.net.URL(identifier.toString());
            } catch (final MalformedURLException e) {
                throw new RDFizerException(
                        "Error: provided identifier '%s' is not a well-formed URL", identifier);
            }
        }
        if (statements.size() == 0) {
            try {
                return (T) new java.net.URI(identifier.toString());
            } catch (final URISyntaxException e) {
                throw new RDFizerException(
                        "Error: provided identifier '%s' is not a well-formed URI", identifier);
            }
        }
        final List<Statement> firstLevelStatements = getLevelOneStatements(identifier, statements);
        // instantiate the object
        T object;
        try {
            object = clazz.newInstance();
        } catch (final InstantiationException e) {
            throw new RDFizerException("Error while instantiating the empty object", e);
        } catch (final IllegalAccessException e) {
            throw new RDFizerException("Error while instantiating the empty object", e);
        }
        // get the RDFIdentifier namespace
        final URI identifierNamespace = getIdentifierPrefix(clazz);
        final String idPart = identifier.toString().replace(identifierNamespace.toString() + "/",
                "");
        // set the identifier
        final Method identifierSetter = getIdentifierSetterMethod(clazz);
        try {
            // should follow the same convention of the normal methods setters
            final ValueTypeHandler vth = (ValueTypeHandler) typeHandlerRegistry
                    .getTypeHandler(identifierSetter.getParameterTypes()[0]);
            Object idPartObject;
            try {
                idPartObject = vth.deserialize(new LiteralImpl(idPart));
            } catch (final TypeHandlerException e) {
                throw new RDFizerException(
                        "Error while gettin the appropriate object for setter: '"
                                + identifierSetter + "'", e);
            }
            set(idPartObject, identifierSetter, object);
        } catch (final IllegalAccessException e) {
            throw new RDFizerException("Error while setting the object identifier", e);
        } catch (final InvocationTargetException e) {
            throw new RDFizerException("Error while setting the object identifier", e);
        } catch (final TypeHandlerRegistryException e) {
            throw new RDFizerException("Error while getting a proper typehander", e);
        }
        // for all the other first level statements
        // all the primitive stuff needed to be set directly
        // recurse on non primitive calling it to the setter
        for (final Statement firstLevelStatement : firstLevelStatements) {
            // skip the type statement
            if (firstLevelStatement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            Object firstLevelStatementObject;
            try {
                firstLevelStatementObject = this.typeHandlerRegistry.getObject(firstLevelStatement);
            } catch (final TypeHandlerRegistryException e) {
                throw new RDFizerException("Error while deserializing", e);
            }
            final URI firstLevelStatmentLiteralSubjectedProperty = firstLevelStatement
                    .getPredicate();
            if (firstLevelStatementObject instanceof java.net.URI) {
                // if it's a URI we have to build the second level objects
                // starting from its id.
                final List<Statement> secondLevelStatements = getLevelOneStatements(
                        (Resource) firstLevelStatement.getObject(), statements);
                if (isMappable(firstLevelStatmentLiteralSubjectedProperty, clazz)) {
                    final List<Method> setters = getSetters(
                            firstLevelStatmentLiteralSubjectedProperty, clazz);
                    for (final Method setter : setters) {
                        final Object secondLevelObject = getObject(secondLevelStatements,
                                (URI) firstLevelStatement.getObject(),
                                setter.getParameterTypes()[0]);
                        try {
                            set(secondLevelObject, setter, object);
                        } catch (final IllegalAccessException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        } catch (final InvocationTargetException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        } catch (final IllegalArgumentException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        }
                    }
                }
            } else {
                if (isMappable(firstLevelStatmentLiteralSubjectedProperty, clazz)) {
                    final List<Method> setters = getSetters(
                            firstLevelStatmentLiteralSubjectedProperty, clazz);
                    for (final Method setter : setters) {
                        try {
                            set(firstLevelStatementObject, setter, object);
                        } catch (final IllegalAccessException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        } catch (final InvocationTargetException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        }
                    }
                }
            }
            
            // get such subjects that are literals
            // for each of them
            // get the property
            // is it mappable to the bean?
            // if yes take the setter and pump it in
        }
        return object;
    }
    
    @Override
    public URI getIdentifierPrefix(final Class clazz) throws RDFizerException {
    
        URI identifier = null;
        final URI rdfNamespace = getRDFNamespace(clazz);
        if (rdfNamespace == null) {
            identifier = getRDFType(clazz);
        } else {
            identifier = rdfNamespace;
        }
        return new URIImpl(identifier.toString());
    }
    
    private boolean isMappable(final URI property, final Class clazz) {
    
        final List<Method> getters = getAnnotatedGetters(clazz);
        for (final Method getter : getters) {
            final List<URI> getterRDFProperties = getRDFProperties(getter);
            if (getterRDFProperties.contains(property))
                return true;
        }
        return false;
    }
    
    private void set(final Object subject, final Method setter, final Object object)
            throws IllegalAccessException, InvocationTargetException {
    
        setter.invoke(object, subject);
    }
    
    private List<Method> getSetters(final URI property, final Class clazz) throws RDFizerException {
    
        final List<Method> getters = getAnnotatedGetters(clazz);
        final List<Method> results = new ArrayList<Method>();
        for (final Method getter : getters) {
            final List<URI> getterRDFProperties = getRDFProperties(getter);
            if (getterRDFProperties.contains(property)) {
                final Class getterReturnType = getter.getReturnType();
                if (isCollection(getterReturnType)) {
                    // then call the delegate
                    final String getterName = getter.getName();
                    final String delegateAddMethodName = getterName.replace("get", "add")
                            .substring(0, getterName.replace("get", "add").length() - 1);
                    for (final Method m : clazz.getMethods()) {
                        if (m.getName().equals(delegateAddMethodName)) {
                            results.add(m);
                            break;
                        }
                    }
                } else {
                    // classical set
                    final String getterName = getter.getName();
                    Method setter;
                    final String setterMethodName = getterName.replace("get", "set");
                    try {
                        setter = clazz.getMethod(setterMethodName, getter.getReturnType());
                    } catch (final NoSuchMethodException e) {
                        throw new RDFizerException("Error: cannot find a method called: '"
                                + setterMethodName + "'", e);
                    }
                    results.add(setter);
                }
            }
        }
        return results;
    }
    
    private Method getIdentifierSetterMethod(final Class clazz) throws RDFizerException {
    
        final Method identifierGetter = getObjectIdentifierGetter(clazz);
        final String identifierGetterName = identifierGetter.getName();
        final String candidateSetterName = identifierGetterName.replace("get", "set");
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(candidateSetterName)) {
                return method;
            }
        }
        throw new RDFizerException("couldnt find identifier setter for class " + clazz.toString());
    }
    
    private Method getObjectIdentifierGetter(final Class clazz) throws RDFizerException {
    
        for (final Method method : clazz.getMethods()) {
            final RDFIdentifier rdfIdentifier = method.getAnnotation(RDFIdentifier.class);
            if (rdfIdentifier != null) {
                return method;
            }
        }
        throw new RDFizerException("Error: it seems that class '" + clazz
                + "' does not have an RDFIdentifier annotation");
    }
    
    private List<Statement> getLevelOneStatements(final Resource subject,
            final List<Statement> statements) {
    
        final List<Statement> result = new ArrayList<Statement>();
        for (final Statement statement : statements) {
            if (statement.getSubject().equals(subject))
                result.add(statement);
        }
        return result;
    }
    
    private List<Statement> getStatements(final Object object, final HashSet<Statement> added)
            throws RDFizerException {
    
        final URI objectRDFType = getRDFType(object.getClass());
        if (objectRDFType == null) {
            throw new RDFizerException("Object: '%s' has no RDFClassType annotation declared",
                    object);
        }
        final URI objectIdentifier = getObjectIdentifier(object);
        if (objectIdentifier == null) {
            throw new RDFizerException("Object: '%s' has no RDFIdentifier annotation declared",
                    object);
        }
        if (added.contains(objectIdentifier)) {
            final Statement[] statementsArray = added.toArray(new Statement[added.size()]);
            return Arrays.asList(statementsArray);
        }
        addStatement(objectIdentifier, RDF.TYPE, objectRDFType, added);
        final List<Method> getters = getAnnotatedGetters(object.getClass());
        for (final Method method : getters) {
            // for each method
            // get the return type
            final Object returnedObject = invokeMethod(method, object);
            final List<URI> rdfProperties = getRDFProperties(method);
            final Class returnType = method.getReturnType();
            if (isHandled(returnType)) {
                // invoke and add, the registry knows how to serialize it
                for (final URI rdfProperty : rdfProperties) {
                    if (isCollection(returnType)) {
                        final Collection roc = (Collection) returnedObject;
                        for (final Object rocObject : roc) {
                            if (isHandled(rocObject.getClass())) {
                                try {
                                    addStatement(objectIdentifier, rdfProperty, rocObject, added);
                                } catch (final TypeHandlerRegistryException e) {
                                    throw new RDFizerException("Error while adding statement", e);
                                }
                            } else {
                                // this means that the object of the collection
                                // is complex: so recurse!
                                final URI rocObjectId = getObjectIdentifier(rocObject);
                                addStatement(objectIdentifier, rdfProperty, rocObjectId, added);
                                getStatements(rocObject, added);
                            }
                        }
                    } else {
                        try {
                            addStatement(objectIdentifier, rdfProperty, returnedObject, added);
                        } catch (final TypeHandlerRegistryException e) {
                            throw new RDFizerException("Error while adding statement", e);
                        }
                    }
                }
            } else if (!isPrimitive(returnType)) {
                // if it is not primitive means that is complex so recurse!
                for (final URI rdfProperty : rdfProperties) {
                    // should manage collections of complex objects
                    if (isCollection(returnType)) {
                        final Collection roc = (Collection) returnedObject;
                        for (final Object rocObject : roc) {
                            if (isHandled(rocObject.getClass())) {
                                try {
                                    addStatement(objectIdentifier, rdfProperty, rocObject, added);
                                } catch (final TypeHandlerRegistryException e) {
                                    throw new RDFizerException("Error while adding statement", e);
                                }
                            } else {
                                // this means that the object of the collection
                                // is complex: so recurse!
                                final URI rocObjectId = getObjectIdentifier(rocObject);
                                addStatement(objectIdentifier, rdfProperty, rocObjectId, added);
                                getStatements(rocObject, added);
                            }
                        }
                    } else {
                        final URI rcoObjectId = getObjectIdentifier(returnedObject);
                        // link between actual object and next
                        addStatement(objectIdentifier, rdfProperty, rcoObjectId, added);
                        // recursion!!
                        getStatements(returnedObject, added);
                    }
                }
            }
        }
        final Statement[] statementsArray = added.toArray(new Statement[added.size()]);
        return Arrays.asList(statementsArray);
    }
    
    private boolean isCollection(final Class clazz) {
    
        final Class[] interfaces = clazz.getInterfaces();
        for (final Class i : interfaces) {
            if (i.equals(Collection.class))
                return true;
        }
        return false;
    }
    
    private List<URI> getRDFProperties(final Method method) {
    
        final RDFProperty rdfProperty = method.getAnnotation(RDFProperty.class);
        final List<URI> uris = new ArrayList<URI>();
        if (rdfProperty != null) {
            final String[] urisS = rdfProperty.properties();
            for (final String uriS : urisS) {
                uris.add(new URIImpl(uriS));
            }
        }
        return uris;
    }
    
    private Object invokeMethod(final Method method, final Object object) throws RDFizerException {
    
        try {
            return method.invoke(object);
        } catch (final IllegalAccessException e) {
            throw new RDFizerException("Error while invoking method: '" + method.getName() + "'", e);
        } catch (final InvocationTargetException e) {
            throw new RDFizerException("Error while invoking method: '" + method.getName() + "'", e);
        }
    }
    
    private boolean isPrimitive(final Class c) {
    
        return c.isPrimitive() || c.equals(Boolean.class) || c.equals(Short.class)
                || c.equals(Integer.class) || c.equals(Long.class) || c.equals(Float.class)
                || c.equals(Byte.class) || c.equals(Date.class) || c.equals(String.class);
    }
    
    private boolean isHandled(final Class clazz) {
    
        if (this.typeHandlerRegistry.getHandledTypes().contains(clazz) || isCollection(clazz)) {
            return true;
        }
        return false;
    }
    
    private List<Method> getAnnotatedGetters(final Class c) {
    
        final List<Method> result = new ArrayList<Method>();
        for (final Method candidate : c.getMethods()) {
            if (candidate.getDeclaringClass().equals(Object.class)) {
                continue;
            }
            if (Modifier.isPublic(candidate.getModifiers())
                    && (candidate.getName().startsWith("is") || candidate.getName().startsWith(
                            "get")) && candidate.getParameterTypes().length == 0
                    && candidate.getReturnType() != Void.class && isAnnotated(candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }
    
    private boolean isAnnotated(final Method method) {
    
        return getRDFProperties(method).size() != 0;
    }
    
    private void addStatement(final URI s, final URI p, final URI o, final HashSet<Statement> added) {
    
        added.add(new StatementImpl(s, p, o));
    }
    
    private void addStatement(final URI s, final URI p, final Object object,
            final HashSet<Statement> added) throws TypeHandlerRegistryException {
    
        if (object != null) {
            final Statement statement = typeHandlerRegistry.getStatement(s, p, object);
            added.add(statement);
        }
    }
    
    private URI getRDFType(final Class beanClass) {
    
        final RDFClassType rdfClassType = (RDFClassType) beanClass
                .getAnnotation(RDFClassType.class);
        if (rdfClassType == null) {
            throw new IllegalArgumentException(String.format(
                    "The class '%s' must specify the '%s' annotation.", beanClass,
                    RDFClassType.class));
        }
        final String classType = rdfClassType.type();
        if (classType.trim().length() == 0) {
            throw new IllegalArgumentException(String.format("Invalid class type '%s'", classType));
        }
        return new URIImpl(classType);
    }
    
    private URI getRDFNamespace(final Class beanClass) {
    
        URIImpl namespace = null;
        final RDFNamespace rdfNamespace = (RDFNamespace) beanClass
                .getAnnotation(RDFNamespace.class);
        
        if (rdfNamespace != null) {
            final String prefix = rdfNamespace.prefix();
            namespace = new URIImpl(prefix);
        }
        return namespace;
    }
    
    private URI getObjectIdentifier(final Object object) throws RDFizerException {
    
        // build instance identifier.
        URI identifier = null;
        final Class beanClass = object.getClass();
        final URI identifierPrefix = getIdentifierPrefix(beanClass);
        
        for (final Method method : beanClass.getMethods()) {
            final RDFIdentifier rdfIdentifier = method.getAnnotation(RDFIdentifier.class);
            if (rdfIdentifier == null) {
                continue;
            }
            if (identifier != null) {
                throw new IllegalArgumentException("Found more than one identifier.");
            }
            Object methodValue;
            try {
                methodValue = method.invoke(object);
            } catch (final Exception e) {
                throw new RuntimeException(format("Error while invoking method %s", method), e);
            }
            identifier = new URIImpl(String.format("%s/%s", identifierPrefix.toString(),
                    methodValue));
        }
        if (identifier == null) {
            throw new IllegalArgumentException(format(
                    "Invalid bean, it is missing a method annotated with: (%s)",
                    RDFIdentifier.class));
        }
        return identifier;
    }
    
}
