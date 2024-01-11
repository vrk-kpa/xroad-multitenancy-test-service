package fi.dvv.xroad.multitenancytestservicesoap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SoapApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private String endpointUrl() {
        return "http://localhost:" + port + contextPath + "/Endpoint";
    };
    
    @Test
    void authenticateAndHelloServiceReturnsGreeting() throws IOException {
        String authenticateRequest = readRequestFile("authenticate-request.xml");

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.TEXT_XML);

        HttpEntity authenticateEntity = new HttpEntity<>(authenticateRequest, header);
        ResponseEntity<String> authenticateResponse = restTemplate.exchange(endpointUrl(), HttpMethod.POST, authenticateEntity, String.class);
        String authenticateBody = authenticateResponse.getBody();
        String jwt = extractJwtFromXmlResponse(authenticateBody);
        assertThat(jwt).isNotNull().isNotEmpty();

        String helloRequest = readRequestFile("hello-service-request.xml");
        helloRequest = helloRequest.replace("dummytoken", jwt);

        HttpEntity helloEntity = new HttpEntity<>(helloRequest, header);
        ResponseEntity<String> helloResponse = restTemplate.exchange(endpointUrl(), HttpMethod.POST, helloEntity, String.class);
        String helloBody = helloResponse.getBody();
        assertThat(helloBody).contains("Erkki Esimerkki");

    }

    private String extractJwtFromXmlResponse(String xmlResponse){
        String[] parts = xmlResponse.split("<extsec:securityToken xmlns:extsec=\"http://x-road.eu/xsd/security-token.xsd\" tokenType=\"urn:ietf:params:oauth:token-type:jwt\">", 2);
        String[] parts2 = parts[1].split("</extsec:securityToken>", 2);
        return parts2[0];
    }

    private String token = "";

    @Test
    void authenticateRequestReturnsSecurityToken() throws IOException {
        String request = readRequestFile("authenticate-request.xml");

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.TEXT_XML);
        HttpEntity entity = new HttpEntity<>(request, header);
        ResponseEntity<String> response = restTemplate.exchange(endpointUrl(), HttpMethod.POST, entity, String.class);
        String responseString = response.getBody();

        token = extractJwtFromXmlResponse(responseString);
        assertThat(token).isNotEmpty();
    }

    @Test
    void helloServiceReturnsUnauthorizedWhenCalledWithoutToken() throws IOException {
        String request = readRequestFile("hello-service-no-token-request.xml");

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.TEXT_XML);
        HttpEntity entity = new HttpEntity<>(request, header);
        ResponseEntity<String> response = restTemplate.exchange(endpointUrl(), HttpMethod.POST, entity, String.class);
        String responseString = response.getBody();

        assertThat(responseString).contains("invalid token");
    }

    @Test
    void helloServiceReturnsUnauthorizedWhenRepresentedPartyDoesNotMatchJwtSub() throws IOException {
        String authenticateRequest = readRequestFile("authenticate-request.xml");

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.TEXT_XML);

        HttpEntity authenticateEntity = new HttpEntity<>(authenticateRequest, header);
        ResponseEntity<String> authenticateResponse = restTemplate.exchange(endpointUrl(), HttpMethod.POST, authenticateEntity, String.class);
        String authenticateBody = authenticateResponse.getBody();
        String jwt = extractJwtFromXmlResponse(authenticateBody);
        assertThat(jwt).isNotNull().isNotEmpty();

        String helloRequest = readRequestFile("hello-service-request.xml");
        helloRequest = helloRequest.replace("dummytoken", jwt);
        helloRequest = helloRequest.replace("MEMBER3", "WRONG_MEMBER");

        HttpEntity helloEntity = new HttpEntity<>(helloRequest, header);
        ResponseEntity<String> helloResponse = restTemplate.exchange(endpointUrl(), HttpMethod.POST, helloEntity, String.class);
        String responseString = helloResponse.getBody();
        assertThat(responseString).contains("invalid token");
    }

    @Test
    void authenticateRequestReturnsToken() throws IOException {

        String request = readRequestFile("authenticate-request.xml");

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.TEXT_XML);
        HttpEntity entity = new HttpEntity<>(request, header);
        ResponseEntity<String> response = restTemplate.exchange(endpointUrl(), HttpMethod.POST, entity, String.class);
        String responseString = response.getBody();

        assertThat(responseString).contains("<extsec:securityToken");
    }

    private String readRequestFile(String filename) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);

        String request = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        return request;
    }
}
