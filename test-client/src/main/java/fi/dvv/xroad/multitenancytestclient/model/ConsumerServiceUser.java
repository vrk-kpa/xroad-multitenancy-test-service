package fi.dvv.xroad.multitenancytestclient.model;

import org.springframework.security.core.userdetails.User;

import java.util.Collections;

public class ConsumerServiceUser extends User {

    private final String xroadMemberClass;
    private final String xroadMemberCode;


    private String token;

    public ConsumerServiceUser(String username, String password, String xroadMemberClass, String xroadMemberCode) {
        super(username, password, Collections.emptySet());
        this.token = null;
        this.xroadMemberClass = xroadMemberClass;
        this.xroadMemberCode = xroadMemberCode;
    }

    public String getXroadMemberClass() {
        return xroadMemberClass;
    }

    public String getXroadMemberCode() {
        return xroadMemberCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
