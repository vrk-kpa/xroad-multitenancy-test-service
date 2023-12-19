package fi.dvv.xroad.multitenancytestclient.auth;

import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.HashMap;

public class ConsumerServiceUser extends User {

    private final String xroadMemberClass;
    private final String xroadMemberCode;
    private HashMap<String, String> tokens = new HashMap<>();
    private final String passwordInSecretsManager;

    public ConsumerServiceUser(String username, String password, String xroadMemberClass, String xroadMemberCode) {
        super(username, password, Collections.emptySet());
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

    public String getToken(String tokenId) {
        return tokens.get(tokenId);
    }

    public String getPasswordFromSecretsManager() {
        return passwordInSecretsManager;
    }

    public void setToken(String tokenId, String token) {
        this.tokens.put(tokenId, token);
    }

}
