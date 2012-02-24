package com.collective.rdfizer.model.hierarchy;

import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.annotations.RDFIdentifier;
import com.collective.rdfizer.annotations.RDFProperty;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
@RDFClassType(type = "http://collective.com/profile/EnhancedResource")
public class EnhancedResource extends SimpleResource {

    private List<URI> topics = new ArrayList<URI>();

    @RDFIdentifier
    public Long getId() {
        return super.getId();
    }

    @RDFProperty(properties = { "http://collective.com/profile/EnhancedResource/title" })
    public String getTitle() {
        return super.getTitle();
    }

    @RDFProperty(properties = { "http://collective.com/profile/EnhancedResource/topic" })
    public List<URI> getTopics() {
        return topics;
    }

    public void setTopics(List<URI> topics) {
        this.topics = topics;
    }

    public void addTopic(URI topic) {
        this.topics.add(topic);
    }
}
