package fi.dvv.xroad.multitenancytestclient.service;

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

    public MessageDto getHello(String name) {
        String uri = securityServerUrl + "/r1/" + serviceId + "/hello?name=" + name;
        System.out.println("Calling security server: " + uri);

        HttpEntity httpEntity = getXroadHttpEntity();
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, MessageDto.class).getBody();
    }

    public RandomNumberDto getRandom() {
        String uri = securityServerUrl + "/r1/" + serviceId + "/random";
        System.out.println("Calling security server: " + uri);

        HttpEntity httpEntity = getXroadHttpEntity();
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, RandomNumberDto.class).getBody();
    }

    private HttpEntity getXroadHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Road-Client", clientId);
        return new HttpEntity(headers);
    }
}
