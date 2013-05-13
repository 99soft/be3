package org.nnsoft.be3.model.uris;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.annotations.RDFIdentifier;
import org.nnsoft.be3.annotations.RDFProperty;
import java.util.UUID;

/**
 * @author Matteo Moci ( matteo (dot) moci (at) gmail (dot) com )
 */
@RDFClassType(type = "http://collective.com/profile/User")
public class User {

    private UUID id;

    private String name;

    private User friend;

    public User(String uuid, String name) {
        this.id = UUID.fromString(uuid);
        this.name = name;
    }

    @RDFProperty(properties = {"http://xmlns.org/01/foaf/name"})
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @RDFIdentifier
    public UUID getId() {
        return id;
    }

    public void setId(String id) {
        this.id = UUID.fromString(id);
    }

    @RDFProperty(properties = {"http://xmlns.org/01/foaf/hasFriend"})
    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

}
