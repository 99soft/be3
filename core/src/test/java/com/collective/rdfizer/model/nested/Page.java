package com.collective.rdfizer.model.nested;

import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.annotations.RDFIdentifier;
import com.collective.rdfizer.annotations.RDFProperty;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
@RDFClassType(type = "http://collective.com/Page")
public class Page {

    private Long number;

    private String content;

    @RDFIdentifier
    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    @RDFProperty(properties = { "http://collective.com/content" })
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return "Page{" +
                "number=" + number +
                ", content='" + content + '\'' +
                '}';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        if (content != null ? !content.equals(page.content) : page.content != null) return false;
        if (number != null ? !number.equals(page.number) : page.number != null) return false;

        return true;
    }

    public int hashCode() {
        int result = number != null ? number.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
