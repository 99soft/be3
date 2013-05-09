package org.nnsoft.be3.model;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.annotations.RDFIdentifier;

@RDFClassType(type = "http://collective.com/Url")
public class MyURL {
    
    private String url;
    
    public MyURL() {
    
    }
    
    public MyURL(final String url) {
    
        this.url = url;
    }
    
    @RDFIdentifier
    public String getUrl() {
    
        return url;
    }
    
    public void setUrl(final String url) {
    
        this.url = url;
    }
}
