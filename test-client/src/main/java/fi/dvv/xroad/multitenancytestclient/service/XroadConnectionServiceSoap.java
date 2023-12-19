package fi.dvv.xroad.multitenancytestclient.service;

import fi.dvv.xroad.multitenancytestclient.auth.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.soap.request.AuthenticateRequest;
import fi.dvv.xroad.multitenancytestclient.soap.request.GetRandomRequest;
import fi.dvv.xroad.multitenancytestclient.soap.request.HelloServiceRequest;
import fi.dvv.xroad.multitenancytestclient.soap.request.XroadSoapRequest;
import fi.dvv.xroad.multitenancytestclient.soap.response.XroadSoapResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
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
        XroadSoapResponse response = makeSoapRequestWithLoginRetry(securityServerUrl, request, principal);
        return new MessageDto(response.getHelloMessage());
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
        XroadSoapResponse response = makeSoapRequestWithLoginRetry(securityServerUrl, request, principal);
        return new RandomNumberDto(response.getRandomNumber());
    }

    private XroadSoapResponse makeSoapRequestWithLoginRetry(
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
            return makeSoapRequest(uri, request, headers);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Access denied")) {
                principal.setToken(TOKEN_ID,null);
                loginPrincipal(principal);
                request.setJwt(principal.getToken(TOKEN_ID));

                return makeSoapRequest(uri, request, headers);

            } else {
                throw e;
            }
        }
    }

    private XroadSoapResponse makeSoapRequest(String uri, XroadSoapRequest request, HttpHeaders headers) {
        HttpEntity<String> httpEntity = new HttpEntity<>(request.toString(), headers);
        String rawResponse = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class).getBody();
        XroadSoapResponse parsedResponse = new XroadSoapResponse(rawResponse);
        String fault = parsedResponse.getFault();
        if(fault != null)
            throw new RuntimeException("Received SOAP fault response: " + fault);
        return parsedResponse;
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
        XroadSoapResponse parsedResponse = new XroadSoapResponse(response);
        String fault = parsedResponse.getFault();
        if(fault != null)
            throw new RuntimeException("Failed to authenticate: " + fault);

        String jwt = parsedResponse.getSecurityToken();
        System.out.println("Got JWT: " + jwt);
        principal.setToken(TOKEN_ID, jwt);
    }
}
