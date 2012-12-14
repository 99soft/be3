package org.nnsoft.be3.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * It defines the namespace of a <i>bean</i> that is used when building
 * the instance identifier when serializing in <i>RDF</>
 * RDFNamespace + VALUE_OF_RDF_IDENTIFIER
 *
 * @author Matteo Moci ( matteo (dot) moci (at) gmail (dot) com )
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RDFNamespace {
    String prefix();
}
