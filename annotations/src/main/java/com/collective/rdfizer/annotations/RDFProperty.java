package com.collective.rdfizer.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It maps a getter method of a <i>bean</i> to one or
 * more <i>RDF</i> properties.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface RDFProperty {

    String[] properties();

}