package fi.dvv.xroad.multitenancytestclient.service;

import fi.dvv.xroad.multitenancytestclient.auth.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class XroadConnectionServiceRest {

    // Used to store this connection's token in the principal's token map
    public static final String TOKEN_ID = "multitenancy-test-service-rest";

    @Value("${security-server.client-id}")
    private String clientId;

    @Value("${security-server.service-id}")
    private String serviceId;

    @Value("${security-server.url}")
    private String securityServerUrl;

    private final RestTemplate restTemplate;

    public XroadConnectionServiceRest(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public MessageDto getHello(ConsumerServiceUser principal, String name) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/private/hello?name=" + name;
        System.out.println("Calling security server: " + uri);

        return restGetWithLoginRetry(uri, MessageDto.class, principal);
    }

    public RandomNumberDto getRandom(ConsumerServiceUser principal) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/private/random";
        System.out.println("Calling security server: " + uri);
        return restGetWithLoginRetry(uri, RandomNumberDto.class, principal);
    }

    private <T> T restGetWithLoginRetry(
            String uri,
            Class<T> responseType,
            ConsumerServiceUser principal
    ) {
        try {
            HttpEntity<T> httpEntity = getXroadHttpEntity(principal);
            return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, responseType).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // The token was invalid, so trigger new login by clearing the principal's token
                principal.setToken(TOKEN_ID,null);
                HttpEntity<T> httpEntity = getXroadHttpEntity(principal);
                return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, responseType).getBody();
            } else {
                throw e;
            }
        }
    }

    private void loginPrincipal(ConsumerServiceUser principal) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/authenticate";
        System.out.println("Calling security server: " + uri);

        HttpHeaders headers = new HttpHeaders();

        headers.set("X-Road-Client", clientId);
        headers.set("X-Road-Represented-Party", principal.getXroadMemberClass() + "/" + principal.getXroadMemberCode());
        headers.set("Member-Username", principal.getXroadMemberClass() + "/" + principal.getXroadMemberCode());
        headers.set("Member-Password", principal.getPasswordFromSecretsManager());

        HttpEntity<MessageDto> entity = new HttpEntity<>(headers);

        System.out.println("Sending headers: " + headers);

        String jwt = restTemplate.exchange(uri, HttpMethod.POST, entity, MessageDto.class).getHeaders().get("Authorization").get(0);
        System.out.println("Got JWT: " + jwt);
        principal.setToken(TOKEN_ID, jwt);
    }

    private <T> HttpEntity<T> getXroadHttpEntity(ConsumerServiceUser principal) {
        if(principal.getToken(TOKEN_ID) == null){
            loginPrincipal(principal);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Road-Client", clientId);
        headers.set("X-Road-Represented-Party", principal.getXroadMemberClass() + "/" + principal.getXroadMemberCode());
        headers.set("Authorization", principal.getToken(TOKEN_ID));
        return new HttpEntity<>(headers);
    }
}
