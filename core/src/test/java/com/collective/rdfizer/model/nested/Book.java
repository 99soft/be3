package com.collective.rdfizer.model.nested;

import com.collective.rdfizer.annotations.RDFClassType;
import com.collective.rdfizer.annotations.RDFIdentifier;
import com.collective.rdfizer.annotations.RDFProperty;

import javax.security.sasl.AuthorizeCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */

@RDFClassType(type = "http://collective.com/Book")
public class Book {

    private Long id;
    private String title;
    private List<Page> pages = new ArrayList<Page>();
    private Author author;

    @RDFIdentifier
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @RDFProperty(properties = { "http://collective.com/profile/title" })
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @RDFProperty(properties = { "http://collective.com/page" })
    public List<Page> getPages() {
        return pages;
    }

    public void addPage(Page page) {
        this.pages.add(page);
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", pages=" + pages +
                ", author=" + author +
                '}';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (author != null ? !author.equals(book.author) : book.author != null) return false;
        if (id != null ? !id.equals(book.id) : book.id != null) return false;
        if (pages != null ? !pages.equals(book.pages) : book.pages != null) return false;
        if (title != null ? !title.equals(book.title) : book.title != null) return false;

        return true;
    }

    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (pages != null ? pages.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        return result;
    }
}
