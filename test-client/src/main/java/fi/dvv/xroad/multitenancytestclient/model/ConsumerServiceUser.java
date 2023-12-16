package fi.dvv.xroad.multitenancytestclient.model;

import org.springframework.security.core.userdetails.User;

import java.util.Collections;

public class ConsumerServiceUser extends User {

    private final String xroadMemberClass;
    private final String xroadMemberCode;
    private String token;
    private String passwordInSecretsManager;

    public ConsumerServiceUser(String username, String password, String xroadMemberClass, String xroadMemberCode) {
        super(username, password, Collections.emptySet());
        this.token = null;
        this.xroadMemberClass = xroadMemberClass;
        this.xroadMemberCode = xroadMemberCode;
        this.passwordInSecretsManager = password;
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

    public String getPasswordFromSecretsManager() {
        return passwordInSecretsManager;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
