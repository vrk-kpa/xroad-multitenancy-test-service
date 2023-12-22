/**
 * The MIT License
 * Copyright © 2018 Nordic Institute for Interoperability Solutions (NIIS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
