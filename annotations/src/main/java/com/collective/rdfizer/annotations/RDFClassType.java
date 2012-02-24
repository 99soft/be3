package com.collective.rdfizer.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * It defines the base <i>URI</i> to identify a bean when it's
 * serialized in <i>RDF</i>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RDFClassType {

    String type();

}