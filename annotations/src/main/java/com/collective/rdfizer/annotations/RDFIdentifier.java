package com.collective.rdfizer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It defines which is the method of a <i>bean</i> that must
 * be used as unique identifier when it's serialized in <i>RDF</i>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RDFIdentifier {}