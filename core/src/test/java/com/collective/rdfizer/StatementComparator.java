package com.collective.rdfizer;

import org.openrdf.model.Statement;

import java.util.Comparator;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
public class StatementComparator implements Comparator<Statement> {

    public int compare(Statement s, Statement o) {
        return s.getSubject().toString().compareTo(o.getSubject().toString());
    }
}
