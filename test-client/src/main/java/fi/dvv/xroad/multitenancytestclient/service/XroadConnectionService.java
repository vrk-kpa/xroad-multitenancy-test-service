package fi.dvv.xroad.multitenancytestclient.service;

import fi.dvv.xroad.multitenancytestclient.model.ConsumerServiceUser;
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
public class XroadConnectionService {

    @Value("${security-server.client-id}")
    private String clientId;

    @Value("${security-server.service-id}")
    private String serviceId;

    @Value("${security-server.url}")
    private String securityServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public MessageDto getHello(ConsumerServiceUser principal) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/private/hello?name=" + principal.getUsername();
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
                principal.setToken(null);
                HttpEntity<T> httpEntity = getXroadHttpEntity(principal);
                return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, responseType).getBody();
            } else {
                throw e;
            }
        }
    }

    private void loginPrincipal(ConsumerServiceUser principal) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/login";
        System.out.println("Calling security server: " + uri);

        HttpHeaders headers = new HttpHeaders();

        headers.set("X-Road-Client", clientId);
        headers.set("X-Road-Represented-Party", principal.getXroadMemberClass() + "/" + principal.getXroadMemberCode());
        headers.set("Member-Username", principal.getXroadMemberClass() + "/" + principal.getXroadMemberCode());
        headers.set("Member-Password", principal.getPasswordFromSecretsManager());

        HttpEntity<MessageDto> entity = new HttpEntity<>(headers);

        System.out.println("Sending headers: " + headers);

        RestTemplate restTemplate = new RestTemplate();
        String jwt = restTemplate.exchange(uri, HttpMethod.GET, entity, MessageDto.class).getHeaders().get("Authorization").get(0);
        System.out.println("Got JWT: " + jwt);
        principal.setToken(jwt);
    }

    private <T> HttpEntity<T> getXroadHttpEntity(ConsumerServiceUser principal) {
        if(principal.getToken() == null){
            loginPrincipal(principal);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Road-Client", clientId);
        headers.set("X-Road-Represented-Party", principal.getXroadMemberClass() + "/" + principal.getXroadMemberCode());
        headers.set("Authorization", principal.getToken());
        return new HttpEntity<>(headers);
    }
}
