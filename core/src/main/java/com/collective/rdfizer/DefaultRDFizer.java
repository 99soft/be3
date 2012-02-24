package com.collective.rdfizer;

import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.annotations.RDFIdentifier;
import com.collective.rdfizer.annotations.RDFProperty;
import com.collective.rdfizer.typehandler.TypeHandlerRegistry;
import org.openrdf.model.*;
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
import java.util.*;

/**
 * Default implementation of {@link com.collective.rdfizer.RDFizer}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Deprecated
public class DefaultRDFizer extends AbstractRDFizer {

    public DefaultRDFizer(Repository repository, TypeHandlerRegistry typeHandlerRegistry) {
        super(repository, typeHandlerRegistry);
    }

    @Override
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
        return getRDFStatements(object, new HashSet<URI>());
    }

    private List<Statement> getRDFStatements(Object object, Set<URI> visited) throws RDFizerException {
        if (object == null) {
            throw new NullPointerException("object parameter cannot be null");
        }
        URI objectRDFType = getRDFType(object.getClass());
        if (objectRDFType == null) {
            throw new RDFizerException("Object: '" + object + "' has no RDFClassType annotation declared");
        }
        URI objectIdentifier = getObjectIdentifier(object);
        if (objectIdentifier == null) {
            throw new RDFizerException("Object: '" + object + "' has no RDFIdentifier annotation declared");
        }
        Set<Statement> statementHeap = new HashSet<Statement>();
        // Returning if already visited: this avoids looping on cyclic beans.
        if (visited.contains(objectIdentifier)) {
            Statement[] statementsArray = statementHeap.toArray(new Statement[statementHeap.size()]);
            return Arrays.asList(statementsArray);
        }
        // else mark it and go on
        visited.add(objectIdentifier);
        addStatement(objectIdentifier, RDF.TYPE, objectRDFType, statementHeap);
        Class objectClass = object.getClass();
        List<Method> getters = getGetters(objectClass);
        for (Method getter : getters) {
            List<URI> objectProperties = getObjectRDFProperty(getter);
            if (objectProperties.size() == 0) {
                continue;
            }
            final Object getterReturnType;
            try {
                getterReturnType = getter.invoke(object);
            } catch (IllegalAccessException e) {
                throw new RDFizerException("Error while invoking method '" + getter + "'", e);
            } catch (InvocationTargetException e) {
                throw new RDFizerException("Error while invoking method '" + getter + "'", e);
            }
            if (getterReturnType == null) {
                continue;
            }
            for (URI objectProperty : objectProperties) {
                if (isPrimitive(getterReturnType)) {
                    Object subjectValue;
                    try {
                        subjectValue = getterReturnType.getClass().cast(getter.invoke(object));
                    } catch (IllegalAccessException e) {
                        throw new RDFizerException("Error while invoking method '" + getter + "'", e);
                    } catch (InvocationTargetException e) {
                        throw new RDFizerException("Error while invoking method '" + getter + "'", e);
                    }
                    addStatement(objectIdentifier, objectProperty, subjectValue, statementHeap);
                } else if (isCollection(getterReturnType)) {
                    Collection<Object> collection;
                    try {
                        collection = (Collection<Object>)
                                getterReturnType.getClass().cast(getter.invoke(object));
                    } catch (IllegalAccessException e) {
                        throw new RDFizerException("Error while invoking method '" + getter + "'", e);
                    } catch (InvocationTargetException e) {
                        throw new RDFizerException("Error while invoking method '" + getter + "'", e);
                    }
                    for (Object innerObject : collection) {
                        if (isPrimitive(innerObject)) {
                            addStatement(objectIdentifier, objectProperty, innerObject, statementHeap);
                        } else {
                            if (isAnnotated(innerObject.getClass())) {
                                for (Statement statement : getRDFStatements(innerObject, visited)) {
                                    addStatement(statement, statementHeap);
                                }
                                addStatement(
                                        objectIdentifier,
                                        objectProperty,
                                        getObjectIdentifier(innerObject),
                                        statementHeap
                                );
                            } else {
                                addStatement(
                                        objectIdentifier,
                                        objectProperty,
                                        innerObject.toString(),
                                        statementHeap);
                            }
                        }
                    }
                } else {
                    for (Statement statement : getRDFStatements(object, visited)) {
                        addStatement(statement, statementHeap);
                    }
                    // addStatement(objectIdentifier, objectProperty, getObjectIdentifier(object), statementHeap);
                    addStatement(objectIdentifier, objectProperty, getterReturnType, statementHeap);
                }
            }
        }


        // check if there is a type
        // if not, throw exception
        // check if there is an id
        // if not throw ex
        // check if there is ONE id
        // if not throw ex

        // take getters
        // for each getter
        // is not annotated ?
        // skip
        // take the annotation
        // is not property ?
        // skip
        // is it primitive ?
        // add statement

        // is it collection ?
        // add property and recurse

        // is it complex ?
        // recurse on object


        Statement[] statementsArray = statementHeap.toArray(new Statement[statementHeap.size()]);
        return Arrays.asList(statementsArray);
    }

    private boolean isAnnotated(Class aClass) {
        try {
            getRDFType(aClass);
        } catch(IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private void addStatement(Statement statement, Set<Statement> alreadyAdded) throws RDFizerException {
        alreadyAdded.add(statement);
    }

    private boolean isCollection(Object getterReturnType) {
        return getterReturnType instanceof Collection;
    }

    private boolean isCollection(Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        for (Class i : interfaces) {
            if (i.equals(Collection.class))
                return true;
        }
        return false;
    }


    private void addStatement(URI objectIdentifier, URI objectProperty, Object subjectValue, Set<Statement> alreadyAdded)
            throws RDFizerException {
        Statement statement = new StatementImpl(
                objectIdentifier,
                objectProperty,
                new LiteralImpl(subjectValue.toString())
        );
        alreadyAdded.add(statement);
    }

    private void addStatement(URI objectIdentifier, URI objectRDFType, URI rdfType, Set<Statement> alreadyAdded) {
        Statement statement = new StatementImpl(objectIdentifier, objectRDFType, rdfType);
        alreadyAdded.add(statement);
    }

    private boolean isPrimitive(Object obj) {
        final Class c = obj.getClass();
        return
                c.isPrimitive()
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
                        obj instanceof String;
    }

    private List<URI> getObjectRDFProperty(Method getter) {
        RDFProperty rdfProperty = getter.getAnnotation(RDFProperty.class);
        List<URI> uris = new ArrayList<URI>();
        if (rdfProperty != null) {
            String[] urisS = rdfProperty.properties();
            for (String uriS : urisS) {
                uris.add(new URIImpl(uriS));
            }
        }
        return uris;
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

    private Method getObjectIdentifierGetter(Class clazz) throws RDFizerException {
        // Find class identifier.
        final URI classType = getRDFType(clazz);
        for (Method method : clazz.getMethods()) {
            RDFIdentifier rdfIdentifier = method.getAnnotation(RDFIdentifier.class);
            if (rdfIdentifier != null) {
                return method;
            }
        }
        throw new RDFizerException("");
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

    private List<Method> getGetters(Class c) {
        List<Method> result = new ArrayList<Method>();
        for (Method candidate : c.getMethods()) {
            if (candidate.getDeclaringClass().equals(Object.class)) {
                continue;
            }
            if ( Modifier.isPublic(candidate.getModifiers())
                            &&
                            (candidate.getName().startsWith("is") || candidate.getName().startsWith("get"))
                            &&
                            candidate.getParameterTypes().length == 0 && candidate.getReturnType() != Void.class
                    ) {
                result.add(candidate);
            }
        }
        return result;
    }

    @Override
    public Object getObject(List<Statement> statements, URI identifier, Class clazz)
            throws RDFizerException {
        // get all the first level statments of identifier
        List<Statement> firstLevelStatements = getLevelOneStatements(identifier, statements);
                
        // instantiate the object
        Object object;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RDFizerException("", e);
        } catch (IllegalAccessException e) {
            throw new RDFizerException("", e);
        }
        // get the RDFIdentifier namespaece
        URI identifierNamespace = getRDFType(clazz);
        String idPart = identifier.toString().replace(identifierNamespace.toString() + "/", "");
        // set the identifier
        Method identifierSetter = getIdentifierSetterMethod(clazz);
        try {
            identifierSetter.invoke(object, idPart);
        } catch (IllegalAccessException e) {
            throw new RDFizerException("", e);
        } catch (InvocationTargetException e) {
            throw new RDFizerException("", e);
        }
        // for all the other first level statements
            // all the primitive stuff needed to be set directly
            // recurse on non primitive calling it to the setter
        for(Statement firstLevelStatement : firstLevelStatements) {
            if(isLiteralObjected(firstLevelStatement)) {
                URI firstLevelStatmentLiteralSubjectedProperty = firstLevelStatement.getPredicate();
                if(isMappable(firstLevelStatmentLiteralSubjectedProperty, clazz)) {
                    List<Method> setters = getSetters(firstLevelStatmentLiteralSubjectedProperty, clazz);
                    for(Method setter : setters) {
                        try {
                            set(firstLevelStatement.getObject(), setter, object);
                        } catch (IllegalAccessException e) {
                            throw new RDFizerException("", e);
                        } catch (InvocationTargetException e) {
                            throw new RDFizerException(", e");
                        }
                    }
                }
            } else {
                // if it's not literal we have to build the second level objects starting from their id. maybe recurse?
                URI objectIdentifier = new URIImpl(firstLevelStatement.getObject().toString());
                List<Statement> secondLevelStatements = getLevelOneStatements(objectIdentifier, statements);
                if (secondLevelStatements.size() != 0) {
                    Object secondLevelObject = getObject(secondLevelStatements, objectIdentifier, clazz);
                    // add this object to the current
                    URI firstLevelStatmentLiteralSubjectedProperty = firstLevelStatement.getPredicate();
                    if (isMappable(firstLevelStatmentLiteralSubjectedProperty, clazz)) {
                        List<Method> setters = getSetters(firstLevelStatmentLiteralSubjectedProperty, clazz);
                        for (Method setter : setters) {
                            try {
                                set(secondLevelObject, setter, object);
                            } catch (IllegalAccessException e) {
                                throw new RDFizerException("", e);
                            } catch (InvocationTargetException e) {
                                throw new RDFizerException(", e");
                            }
                        }
                    }
                } else {
                    URI firstLevelStatmentLiteralSubjectedProperty = firstLevelStatement.getPredicate();
                    if (isMappable(firstLevelStatmentLiteralSubjectedProperty, clazz)) {
                        List<Method> setters = getSetters(firstLevelStatmentLiteralSubjectedProperty, clazz);
                        for (Method setter : setters) {
                            try {
                                set(firstLevelStatement.getObject().stringValue(), setter, object);
                            } catch (IllegalAccessException e) {
                                throw new RDFizerException("", e);
                            } catch (InvocationTargetException e) {
                                throw new RDFizerException(", e");
                            }
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

    private void set(Value subject, Method setter, Object object) 
            throws IllegalAccessException, InvocationTargetException {
        setter.invoke(object, subject.stringValue());
    }

    private void set(Object subject, Method setter, Object object)
            throws IllegalAccessException, InvocationTargetException {
        setter.invoke(object, subject);
    }


    private List<Method> getSetters(URI property, Class clazz) throws RDFizerException {
        List<Method> getters = getGetters(clazz);
        List<Method> results = new ArrayList<Method>();
        for(Method getter : getters) {
            List<URI> getterRDFProperties = getObjectRDFProperty(getter);
            if(getterRDFProperties.contains(property)) {
                Class getterReturnType = getter.getReturnType();
                if (isCollection(getterReturnType)) {
                    // then call the delegate
                    String getterName = getter.getName();
                    Method setter;
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
                    try {
                        String setterMethodName = getterName.replace(
                                "get", "set"
                        );
                        // setter = clazz.getMethod(setterMethodName, getter.getReturnType());
                        // TODO (high) handle types
                        setter = clazz.getMethod(setterMethodName, String.class);
                    } catch (NoSuchMethodException e) {
                        throw new RDFizerException(e.getMessage(), e);
                    }
                    results.add(setter);
                }
            }
        }
        return results;
    }

    private boolean isMappable(URI property, Class clazz) {
        List<Method> getters = getGetters(clazz);
        for(Method getter : getters) {
            List<URI> getterRDFProperties = getObjectRDFProperty(getter);
            if(getterRDFProperties.contains(property))
                return true;
        }
        return false;
    }

    private boolean isLiteralObjected(Statement statement) {
        return statement.getObject() instanceof Literal;
    }

    private List<Statement> getLevelOneStatements(Resource subject, List<Statement> statements) {
        List<Statement> result = new ArrayList<Statement>();
        for(Statement statement : statements) {
            if(statement.getSubject().equals(subject))
                result.add(statement);
        }
        return result;
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

}
