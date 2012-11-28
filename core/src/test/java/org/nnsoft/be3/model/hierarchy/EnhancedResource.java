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
package org.nnsoft.be3.model.hierarchy;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.annotations.RDFIdentifier;
import org.nnsoft.be3.annotations.RDFProperty;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
@RDFClassType(type = "http://collective.com/profile/EnhancedResource")
public class EnhancedResource extends SimpleResource {

    private List<URI> topics = new ArrayList<URI>();

    @Override
    @RDFIdentifier
    public Long getId() {
        return super.getId();
    }

    @Override
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
