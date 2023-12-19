package fi.dvv.xroad.multitenancytestclient.service;

import fi.dvv.xroad.multitenancytestclient.model.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.model.soaprequest.AuthenticateRequest;
import fi.dvv.xroad.multitenancytestclient.model.soaprequest.GetRandomRequest;
import fi.dvv.xroad.multitenancytestclient.model.soaprequest.HelloServiceRequest;
import fi.dvv.xroad.multitenancytestclient.model.soaprequest.XroadSoapRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class XroadConnectionServiceSoap {

    // Used to store this connection's token in the principal's token map
    public static final String TOKEN_ID = "multitenancy-test-service-soap";

    @Value("${security-server.client-id}")
    private String clientId;

    @Value("${security-server.service-id}")
    private String serviceId;

    @Value("${security-server.url}")
    private String securityServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public MessageDto makeHelloServiceRequest(ConsumerServiceUser principal, String name) {
        System.out.println("Sending helloService request to security server: " + securityServerUrl);
        XroadSoapRequest request = new HelloServiceRequest(
                clientId,
                serviceId,
                principal.getXroadMemberClass(),
                principal.getXroadMemberCode(),
                principal.getToken(TOKEN_ID),
                name
        );
        String response = makeSoapRequestWithLoginRetry(securityServerUrl, request, principal);
        return new MessageDto(extractHelloMessageFromResponse(response));
    }

    public RandomNumberDto makeGetRandomRequest(ConsumerServiceUser principal) {
        System.out.println("Sending getRandom request to security server: " + securityServerUrl);
        XroadSoapRequest request = new GetRandomRequest(
                clientId,
                serviceId,
                principal.getXroadMemberClass(),
                principal.getXroadMemberCode(),
                principal.getToken(TOKEN_ID)
        );
        String response = makeSoapRequestWithLoginRetry(securityServerUrl, request, principal);
        return new RandomNumberDto(extractRandomNumberFromResponse(response));
    }

    private String makeSoapRequestWithLoginRetry(
            String uri,
            XroadSoapRequest request,
            ConsumerServiceUser principal
    ) {

        if(principal.getToken(TOKEN_ID) == null || request.getJwt() == null){
            principal.setToken(TOKEN_ID,null);
            loginPrincipal(principal);
            request.setJwt(principal.getToken(TOKEN_ID));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(request.toString(), headers);
            return restTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                principal.setToken(TOKEN_ID,null);
                loginPrincipal(principal);
                request.setJwt(principal.getToken(TOKEN_ID));
                HttpEntity<String> httpEntity = new HttpEntity<>(request.toString(), headers);
                return restTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class).getBody();
            } else {
                throw e;
            }
        }
    }

    private void loginPrincipal(ConsumerServiceUser principal) {

        System.out.println("Sending authenticate request to security server: " + securityServerUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        XroadSoapRequest request = new AuthenticateRequest(
                clientId,
                serviceId,
                principal.getXroadMemberClass(),
                principal.getXroadMemberCode(),
                principal.getPasswordFromSecretsManager()
        );

        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);

        String response = restTemplate.exchange(securityServerUrl, HttpMethod.POST, entity, String.class).getBody();
        String jwt = extractJwtFromResponse(response);
        System.out.println("Got JWT: " + jwt);
        principal.setToken(TOKEN_ID, jwt);
    }

    private String extractJwtFromResponse(String response){
        System.out.println("Got response: " + response);
        // Should do this with a real XML parser but here we go...
        String[] parts = response.split("<extsec:securityToken xmlns:extsec=\"http://x-road.eu/xsd/security-token.xsd\" tokenType=\"urn:ietf:params:oauth:token-type:jwt\">", 2);
        String[] parts2 = parts[1].split("</extsec:securityToken>", 2);
        return parts2[0];
    }

    private int extractRandomNumberFromResponse(String response){
        System.out.println("Got response: " + response);
        String[] parts = response.split("<ts1:data>", 2);
        String[] parts2 = parts[1].split("</ts1:data>", 2);
        return Integer.parseInt(parts2[0]);
    }

    private String extractHelloMessageFromResponse(String response){
        System.out.println("Got response: " + response);
        String[] parts = response.split("<ts1:message>", 2);
        String[] parts2 = parts[1].split("</ts1:message>", 2);
        return parts2[0];
    }
}
