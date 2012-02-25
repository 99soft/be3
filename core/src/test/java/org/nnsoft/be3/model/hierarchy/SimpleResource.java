package org.nnsoft.be3.model.hierarchy;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
public class SimpleResource {

    private Long id;

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
