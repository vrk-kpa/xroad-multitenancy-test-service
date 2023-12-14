package fi.dvv.xroad.multitenancytestclient.service;

import fi.dvv.xroad.multitenancytestclient.model.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class XroadConnectionService {

    @Value("${security-server.client-id}")
    private String clientId;

    @Value("${security-server.service-id}")
    private String serviceId;

    @Value("${security-server.url}")
    private String securityServerUrl;

    public MessageDto getHello(ConsumerServiceUser principal) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/private/hello?name=" + principal.getUsername();
        System.out.println("Calling security server: " + uri);

        HttpEntity httpEntity = getXroadHttpEntity(principal);
        httpEntity.getHeaders().set("Authorization", principal.getToken());
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, MessageDto.class).getBody();
    }

    public RandomNumberDto getRandom(ConsumerServiceUser principal) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/private/random";
        System.out.println("Calling security server: " + uri);

        HttpEntity httpEntity = getXroadHttpEntity(principal);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, RandomNumberDto.class).getBody();
    }

    private void loginPrincipal(ConsumerServiceUser principal) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/login";
        System.out.println("Calling security server: " + uri);

        HttpHeaders headers = new HttpHeaders();

        headers.set("X-Road-Client", clientId);
        headers.set("X-Road-Represented-Party", principal.getXroadMemberClass() + "/" + principal.getXroadMemberCode());
        headers.set("Member-Username", principal.getXroadMemberClass() + "/" + principal.getXroadMemberCode());
        headers.set("Member-Password", "password");

        HttpEntity entity = new HttpEntity(headers);

        System.out.println("Sending headers: " + headers);

        RestTemplate restTemplate = new RestTemplate();
        String jwt = restTemplate.exchange(uri, HttpMethod.GET, entity, MessageDto.class).getHeaders().get("Authorization").get(0);
        System.out.println("Got JWT: " + jwt);
        principal.setToken(jwt);
    }

    private HttpEntity getXroadHttpEntity(ConsumerServiceUser principal) {
        if(principal.getToken() == null){
            loginPrincipal(principal);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Road-Client", clientId);
        headers.set("Authorization", principal.getToken());
        return new HttpEntity(headers);
    }
}
