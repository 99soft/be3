package com.collective.rdfizer;

import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.annotations.RDFIdentifier;
import com.collective.rdfizer.annotations.RDFProperty;
import com.collective.rdfizer.typehandler.TypeHandlerException;
import com.collective.rdfizer.typehandler.TypeHandlerRegistry;
import com.collective.rdfizer.typehandler.TypeHandlerRegistryException;
import com.collective.rdfizer.typehandler.ValueTypeHandler;
import org.apache.log4j.Logger;
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

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TypedRDFizer extends AbstractRDFizer {

    private static final Logger logger = Logger.getLogger(TypedRDFizer.class);

    public TypedRDFizer(Repository repository, TypeHandlerRegistry typeHandlerRegistry) {
        super(repository, typeHandlerRegistry);
    }

    public void serialize(Object object, OutputStream outputStream, Format format)
            throws RDFizerException {
        List<Statement> statements = getRDFStatements(object);
        try {
            super.repository.getConnection().add(statements);
            super.repository.getConnection().commit();
        } catch (RepositoryException e) {
            throw new RDFizerException("Error while adding RDF Statements of object '" + object + "'", e);
        }
        try {
            super.repository.getConnection().export(new RDFXMLPrettyWriter(outputStream));
            super.repository.getConnection().clear((Resource) null);
            super.repository.getConnection().commit();
            super.repository.getConnection().close();
        } catch (RepositoryException e) {
            throw new RDFizerException("Error while producing RDF/XML serialization", e);
        } catch (RDFHandlerException e) {
            throw new RDFizerException("Error while producing RDF/XML serialization", e);
        }
    }

    public List<Statement> getRDFStatements(Object object) throws RDFizerException {
        return getStatements(object, new HashSet<Statement>());
    }

    public Object getObject(List<Statement> statements, URI identifier, Class clazz)
            throws RDFizerException {
        if(clazz.equals(java.net.URI.class)) {
            try {
                return new java.net.URI(identifier.toString());
            } catch (URISyntaxException e) {
                throw new RDFizerException("Error: provided identifier '"
                        + identifier + "' is not a ell-formed URI", e);
            }
        }
        if(clazz.equals(java.net.URL.class)) {
            try {
                return new java.net.URL(identifier.toString());
            } catch (MalformedURLException e) {
                throw new RDFizerException("Error: provided identifier '"
                        + identifier + "' is not a ell-formed URL", e);
            }
        }
        if(statements.size() == 0) {
            try {
                return new java.net.URI(identifier.toString());
            } catch (URISyntaxException e) {
                throw new RDFizerException("Error: provided identifier '"
                        + identifier + "' is not a ell-formed URI", e);
            }
        }
        List<Statement> firstLevelStatements = getLevelOneStatements(identifier, statements);
        // instantiate the object
        Object object;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RDFizerException("Error while instantiating the empty object", e);
        } catch (IllegalAccessException e) {
            throw new RDFizerException("Error while instantiating the empty object", e);
        }
        // get the RDFIdentifier namespaece
        URI identifierNamespace = getRDFType(clazz);
        String idPart = identifier.toString().replace(identifierNamespace.toString() + "/", "");
        // set the identifier
        Method identifierSetter = getIdentifierSetterMethod(clazz);
        try {
            // should follow the same convention of the normal methods setters
            ValueTypeHandler vth = (ValueTypeHandler)
                    typeHandlerRegistry.getTypeHandler(identifierSetter.getParameterTypes()[0]);
            Object idPartObject;
            try {
                idPartObject = vth.deserialize(new LiteralImpl(idPart));
            } catch (TypeHandlerException e) {
                throw new RDFizerException(
                        "Error while gettin the appropriate object for setter: '" + identifierSetter  + "'", e);
            }
            set(idPartObject, identifierSetter, object);
        } catch (IllegalAccessException e) {
            throw new RDFizerException("Error while setting the object identifier", e);
        } catch (InvocationTargetException e) {
            throw new RDFizerException("Error while setting the object identifier", e);
        } catch (TypeHandlerRegistryException e) {
            throw new RDFizerException("Error while getting a proper typehander", e);
        }
        // for all the other first level statements
        // all the primitive stuff needed to be set directly
        // recurse on non primitive calling it to the setter
        for (Statement firstLevelStatement : firstLevelStatements) {
            // skip the type statement
            if(firstLevelStatement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            Object firstLevelStatementObject;
            try {
                firstLevelStatementObject =
                        this.typeHandlerRegistry.getObject(firstLevelStatement);
            } catch (TypeHandlerRegistryException e) {
                throw new RDFizerException("Error while deserializing", e);
            }
            URI firstLevelStatmentLiteralSubjectedProperty = firstLevelStatement.getPredicate();
            if (firstLevelStatementObject instanceof java.net.URI) {
                // if it's a URI we have to build the second level objects
                // starting from its id.
                List<Statement> secondLevelStatements = getLevelOneStatements
                        ((Resource) firstLevelStatement.getObject(), statements);
                if (isMappable(firstLevelStatmentLiteralSubjectedProperty, clazz)) {
                    List<Method> setters = getSetters(firstLevelStatmentLiteralSubjectedProperty, clazz);
                    for (Method setter : setters) {
                        Object secondLevelObject = getObject(
                                secondLevelStatements,
                                (URI) firstLevelStatement.getObject(),
                                setter.getParameterTypes()[0]
                        );
                        try {
                            set(secondLevelObject, setter, object);
                        } catch (IllegalAccessException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        } catch (InvocationTargetException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        } catch (IllegalArgumentException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        }
                    }
                }
            } else {
                if (isMappable(firstLevelStatmentLiteralSubjectedProperty, clazz)) {
                    List<Method> setters = getSetters(firstLevelStatmentLiteralSubjectedProperty, clazz);
                    for (Method setter : setters) {
                        try {
                            set(firstLevelStatementObject, setter, object);
                        } catch (IllegalAccessException e) {
                            throw new RDFizerException("Error while setting the bean", e);
                        } catch (InvocationTargetException e) {
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

    private boolean isMappable(URI property, Class clazz) {
        List<Method> getters = getAnnotatedGetters(clazz);
        for(Method getter : getters) {
            List<URI> getterRDFProperties = getRDFProperties(getter);
            if(getterRDFProperties.contains(property))
                return true;
        }
        return false;
    }

     private void set(Object subject, Method setter, Object object)
            throws IllegalAccessException, InvocationTargetException {
        setter.invoke(object, subject);
    }


    private List<Method> getSetters(URI property, Class clazz) throws RDFizerException {
        List<Method> getters = getAnnotatedGetters(clazz);
        List<Method> results = new ArrayList<Method>();
        for(Method getter : getters) {
            List<URI> getterRDFProperties = getRDFProperties(getter);
            if(getterRDFProperties.contains(property)) {
                Class getterReturnType = getter.getReturnType();
                if (isCollection(getterReturnType)) {
                    // then call the delegate
                    String getterName = getter.getName();
                    String delegateAddMethodName = getterName.replace(
                            "get", "add").substring(0, getterName.replace("get", "add").length() - 1
                    );
                    for (Method m : clazz.getMethods()) {
                        if (m.getName().equals(delegateAddMethodName)) {
                            results.add(m);
                            break;
                        }
                    }
                } else {
                    // classical set
                    String getterName = getter.getName();
                    Method setter;
                    String setterMethodName = getterName.replace(
                            "get", "set"
                    );
                    try {
                        setter = clazz.getMethod(setterMethodName, getter.getReturnType());
                    } catch (NoSuchMethodException e) {
                        throw new RDFizerException("Error: cannot find a method called: '" +
                                setterMethodName + "'", e);
                    }
                    results.add(setter);
                }
            }
        }
        return results;
    }

    private Method getIdentifierSetterMethod(Class clazz) throws RDFizerException {
        Method identifierGetter = getObjectIdentifierGetter(clazz);
        String identifierGetterName = identifierGetter.getName();
        String candidateSetterName = identifierGetterName.replace("get", "set");
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(candidateSetterName)) {
                return method;
            }
        }
        throw new RDFizerException("");
    }

    private Method getObjectIdentifierGetter(Class clazz) throws RDFizerException {
        // Find class identifier.
        final URI classType = getRDFType(clazz);
        for (Method method : clazz.getMethods()) {
            RDFIdentifier rdfIdentifier = method.getAnnotation(RDFIdentifier.class);
            if (rdfIdentifier != null) {
                return method;
            }
        }
        throw new RDFizerException("Error: it seems that class '" + clazz
                + "' does not have an RDFIdentifier annotation");
    }

    private List<Statement> getLevelOneStatements(Resource subject, List<Statement> statements) {
        List<Statement> result = new ArrayList<Statement>();
        for(Statement statement : statements) {
            if(statement.getSubject().equals(subject))
                result.add(statement);
        }
        return result;
    }

    private List<Statement> getStatements(Object object, HashSet<Statement> added)
            throws RDFizerException {
        URI objectRDFType = getRDFType(object.getClass());
        if (objectRDFType == null) {
            throw new RDFizerException("Object: '" + object + "' has no RDFClassType annotation declared");
        }
        URI objectIdentifier = getObjectIdentifier(object);
        if (objectIdentifier == null) {
            throw new RDFizerException("Object: '" + object + "' has no RDFIdentifier annotation declared");
        }
        if (added.contains(objectIdentifier)) {
            Statement[] statementsArray = added.toArray(new Statement[added.size()]);
            return Arrays.asList(statementsArray);
        }
        addStatement(objectIdentifier, RDF.TYPE, objectRDFType, added);
        List<Method> getters = getAnnotatedGetters(object.getClass());
        for(Method method : getters) {
            // for each method
            // get the return type
            Object returnedObject = invokeMethod(method, object);
            List<URI> rdfProperties = getRDFProperties(method);
            Class returnType = method.getReturnType();
            if(isHandled(returnType)) {
                // invoke and add, the registry knows how to serialize it
                for(URI rdfProperty : rdfProperties) {
                    if(isCollection(returnType)) {
                        Collection roc = (Collection) returnedObject;
                        for(Object rocObject : roc) {
                            if(isHandled(rocObject.getClass())) {
                                try {
                                    addStatement(objectIdentifier, rdfProperty, rocObject, added);
                                } catch (TypeHandlerRegistryException e) {
                                    throw new RDFizerException("Error while adding statement", e);
                                }
                            } else {
                                // this means that the object of the collection is complex: so recurse!
                                URI rocObjectId = getObjectIdentifier(rocObject);
                                addStatement(objectIdentifier, rdfProperty, rocObjectId, added);
                                getStatements(rocObject, added);
                            }
                        }
                    } else {
                        try {
                            addStatement(objectIdentifier, rdfProperty, returnedObject, added);
                        } catch (TypeHandlerRegistryException e) {
                            throw new RDFizerException("Error while adding statement", e);
                        }
                    }
                }
            } else if(!isPrimitive(returnType)) {
                // if it is not primitive means that is complex so recurse!
                for (URI rdfProperty : rdfProperties) {
                    //should manage collections of complex objects
                    if(isCollection(returnType)) {
                        Collection roc = (Collection) returnedObject;
                        for(Object rocObject : roc) {
                            if(isHandled(rocObject.getClass())) {
                                try {
                                    addStatement(objectIdentifier, rdfProperty, rocObject, added);
                                } catch (TypeHandlerRegistryException e) {
                                    throw new RDFizerException("Error while adding statement", e);
                                }
                            } else {
                                // this means that the object of the collection is complex: so recurse!
                                URI rocObjectId = getObjectIdentifier(rocObject);
                                addStatement(objectIdentifier, rdfProperty, rocObjectId, added);
                                getStatements(rocObject, added);
                            }
                        }
                    } else {
                        URI rcoObjectId = getObjectIdentifier(returnedObject);
                        //link between actual object and next
                        addStatement(objectIdentifier, rdfProperty, rcoObjectId, added);
                        //recursion!!
                        getStatements(returnedObject, added);
                    }
                }
            }
        }
        Statement[] statementsArray = added.toArray(new Statement[added.size()]);
        return Arrays.asList(statementsArray);
    }

    private boolean isCollection(Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        for (Class i : interfaces) {
            if (i.equals(Collection.class))
                return true;
        }
        return false;
    }

    private List<URI> getRDFProperties(Method method) {
        RDFProperty rdfProperty = method.getAnnotation(RDFProperty.class);
        List<URI> uris = new ArrayList<URI>();
        if (rdfProperty != null) {
            String[] urisS = rdfProperty.properties();
            for (String uriS : urisS) {
                uris.add(new URIImpl(uriS));
            }
        }
        return uris;
    }

    private Object invokeMethod(Method method, Object object) throws RDFizerException {
        try {
            return method.invoke(object);
        } catch (IllegalAccessException e) {
            throw new RDFizerException("Error while invoking method: '" + method.getName() + "'", e);
        } catch (InvocationTargetException e) {
            throw new RDFizerException("Error while invoking method: '" + method.getName() + "'", e);            
        }
    }

    private boolean isPrimitive(Class c) {
        return  c.isPrimitive()
                        ||
                        c.equals(Boolean.class)
                        ||
                        c.equals(Short.class)
                        ||
                        c.equals(Integer.class)
                        ||
                        c.equals(Long.class)
                        ||
                        c.equals(Float.class)
                        ||
                        c.equals(Byte.class)
                        ||
                        c.equals(Date.class)
                        ||
                        c.equals(String.class);
    }

    private boolean isHandled(Class clazz) {
        if(this.typeHandlerRegistry.getHandledTypes().contains(clazz) || isCollection(clazz)) {
            return true;
        }
        return false;
    }

    private List<Method> getAnnotatedGetters(Class c) {
        List<Method> result = new ArrayList<Method>();
        for (Method candidate : c.getMethods()) {
            if (candidate.getDeclaringClass().equals(Object.class)) {
                continue;
            }
            if ( Modifier.isPublic(candidate.getModifiers())
                            &&
                            (candidate.getName().startsWith("is") || candidate.getName().startsWith("get"))
                            &&
                            candidate.getParameterTypes().length == 0
                            && candidate.getReturnType() != Void.class
                            && isAnnotated(candidate)
                    ) {
                result.add(candidate);
            }
        }
        return result;
    }

    private boolean isAnnotated(Method method) {
        return getRDFProperties(method).size() != 0;
    }

    private void addStatement(URI s, URI p, URI o, HashSet<Statement> added) {
        added.add(new StatementImpl(s, p, o));
    }

    private void addStatement(URI s, URI p, Object object, HashSet<Statement> added)
            throws TypeHandlerRegistryException {
        Statement statement = typeHandlerRegistry.getStatement(s, p, object);
        added.add(statement);
    }

    private URI getRDFType(Class beanClass) {
        RDFClassType rdfClassType = (RDFClassType) beanClass.getAnnotation(RDFClassType.class);
        if (rdfClassType == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "The class '%s' must specify the '%s' annotation.",
                            beanClass,
                            RDFClassType.class
                    )
            );
        }
        final String classType = rdfClassType.type();
        if (classType.trim().length() == 0) {
            throw new IllegalArgumentException(String.format("Invalid class type '%s'", classType));
        }
        return new URIImpl(classType);
    }

    private URI getObjectIdentifier(Object object) throws RDFizerException {
        // Find class identifier.
        URI identifier = null;
        final Class beanClass = object.getClass();
        final URI classType = getRDFType(beanClass);
        for (Method method : beanClass.getMethods()) {
            RDFIdentifier rdfIdentifier = method.getAnnotation(RDFIdentifier.class);
            if (rdfIdentifier == null) {
                continue;
            }
            if (identifier != null) {
                throw new IllegalArgumentException("Found more than one identifier.");
            }
            Object methodValue;
            try {
                methodValue = method.invoke(object);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Error while invoking method %s", method), e);
            }
            identifier = new URIImpl(String.format("%s/%s", classType.toString(), methodValue));
        }
        if (identifier == null) {
            throw new IllegalArgumentException(
                    String.format("Invalid bean, it is missing an identifier method. (%s)", RDFIdentifier.class)
            );
        }
        return identifier;
    }

}
