package org.nnsoft.be3.model;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.annotations.RDFIdentifier;
import org.nnsoft.be3.annotations.RDFProperty;

/**
 * @author Matteo Moci ( matteo (dot) moci (at) gmail (dot) com )
 */
@RDFClassType(type = "http://a.namespace.com/Item")
public class Item {

    private String id;

    private Long length;

    public Item(String id, Long length) {

        this.id = id;
        this.length = length;
    }

    @RDFIdentifier
    public String getId() {

        return id;
    }

    @RDFProperty(properties = "http://a.namespace.com/hasLength")
    public Long getLength() {

        return length;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Item item = (Item) o;

        if (id != null ? !id.equals(item.id) : item.id != null) {
            return false;
        }
        if (length != null ? !length.equals(item.length) : item.length != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (length != null ? length.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {

        return "Item{" +
               "id='" + id + '\'' +
               ", length=" + length +
               '}';
    }
}
