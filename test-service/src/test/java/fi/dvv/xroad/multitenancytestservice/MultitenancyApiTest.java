package fi.dvv.xroad.multitenancytestservice;

import fi.dvv.xroad.multitenancytestservice.model.ErrorDto;
import fi.dvv.xroad.multitenancytestservice.model.MessageDto;
import fi.dvv.xroad.multitenancytestservice.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestservice.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;


import java.net.URI;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MultitenancyApiTest {

    @LocalServerPort
    private int port;

    @Value("${server.servlet.contextPath}")
    private String contextPath;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    private String baseUrl() {
        return "http://localhost:" + port + contextPath;
    };

    @Test
    void getGreetingWithoutJwtReturnsUnauthorized() throws Exception {
        String randomUrl = "http://localhost:" + port + contextPath + "/private/message";
        ErrorDto error = restTemplate.getForObject(randomUrl, ErrorDto.class);
        assertThat(error.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void getRandomWithoutJwtReturnsUnauthorized() throws Exception {
        String randomUrl = "http://localhost:" + port + contextPath + "/private/random";
        ErrorDto error = restTemplate.getForObject(randomUrl, ErrorDto.class);
        assertThat(error.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void getRandomWithNonsenseJwtReturnsUnauthorized() throws Exception {
        String randomUrl = "http://localhost:" + port + contextPath + "/private/random";
        HttpHeaders randomHeaders = new HttpHeaders();
        randomHeaders.set("Authorization", "Bearer foobar");
        HttpEntity randomEntity = new HttpEntity(randomHeaders);
        ErrorDto error = restTemplate.exchange(randomUrl, HttpMethod.GET, randomEntity, ErrorDto.class).getBody();
        assertThat(error.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void getRandomWithExpiredJwtReturnsUnauthorized() throws Exception {
        String jwt = jwtService.generateJwt("FOO/12345-6", Date.from(Instant.now().minusSeconds(60*60*2)));

        String randomUrl = "http://localhost:" + port + contextPath + "/private/random";
        HttpHeaders randomHeaders = new HttpHeaders();
        randomHeaders.set("Authorization", "Bearer " + jwt);
        randomHeaders.set("X-Road-Represented-Party", "FOO/12345-6");
        HttpEntity randomEntity = new HttpEntity(randomHeaders);
        ErrorDto error = restTemplate.exchange(randomUrl, HttpMethod.GET, randomEntity, ErrorDto.class).getBody();
        assertThat(error.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void getRandomWithInvalidJwtSubjectReturnsUnauthorized() throws Exception {
        String jwt = jwtService.generateJwt("FOO/12345-6", Date.from(Instant.now().plusSeconds(60*60*2 /* == 2 hours */)));

        String randomUrl = "http://localhost:" + port + contextPath + "/private/random";
        HttpHeaders randomHeaders = new HttpHeaders();
        randomHeaders.set("Authorization", "Bearer " + jwt);
        randomHeaders.set("X-Road-Represented-Party", "BAR/456789-6");
        HttpEntity randomEntity = new HttpEntity(randomHeaders);
        ErrorDto error = restTemplate.exchange(randomUrl, HttpMethod.GET, randomEntity, ErrorDto.class).getBody();
        assertThat(error.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void getLoginReturnsJwt() throws Exception {
        String loginUrl = baseUrl() + "/authenticate";

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.set("X-Road-Represented-Party", "FOO/12345-6");
        loginHeaders.set("Member-Username", "FOO/12345-6");
        loginHeaders.set("Member-Password", "password");

        HttpEntity loginEntity = new HttpEntity(loginHeaders);
        HttpEntity loginResponse = restTemplate.exchange(loginUrl, HttpMethod.POST, loginEntity, String.class);

        String authHeader = loginResponse.getHeaders().get("Authorization").get(0);
        assertThat(authHeader).contains("Bearer ");

        String jwt = authHeader.split(" ")[1];
        assertThat(jwtService.validateJwt(jwt, "FOO/12345-6")).isTrue();
    }


    @Test
    void getRandomWithJwtReturnsRandomNumber() throws Exception {
        String jwt = jwtService.generateJwt("FOO/12345-6", Date.from(Instant.now().plusSeconds(60*60*2 /* == 2 hours */)));

        String randomUrl = "http://localhost:" + port + contextPath + "/private/random";
        HttpHeaders randomHeaders = new HttpHeaders();
        randomHeaders.set("Authorization", "Bearer " + jwt);
        randomHeaders.set("X-Road-Represented-Party", "FOO/12345-6");
        HttpEntity randomEntity = new HttpEntity(randomHeaders);
        RandomNumberDto randomNumberDto = restTemplate.exchange(randomUrl, HttpMethod.GET, randomEntity, RandomNumberDto.class).getBody();
        assertThat(randomNumberDto.data()).isGreaterThanOrEqualTo(1).isLessThanOrEqualTo(100);
    }

    @Test
    void getHelloWithJwtReturnsGreetingWithSubject() throws Exception {
        String jwt = jwtService.generateJwt("FOO/12345-6", Date.from(Instant.now().plusSeconds(60*60*2 /* == 2 hours */)));

        URI helloUrl = new URI("http://localhost:" + port + contextPath + "/private/hello?name=Erkki%20Esimerkki");
        HttpHeaders helloHeaders = new HttpHeaders();
        helloHeaders.set("Authorization", "Bearer " + jwt);
        helloHeaders.set("X-Road-Represented-Party", "FOO/12345-6");
        HttpEntity helloEntity = new HttpEntity(helloHeaders);
        MessageDto helloResponse = restTemplate.exchange(helloUrl, HttpMethod.GET, helloEntity, MessageDto.class).getBody();
        assertThat(helloResponse.message()).contains("FOO/12345-6");
        assertThat(helloResponse.message()).contains("Erkki Esimerkki");
    }

    @Test
    void jwksReturnsPublicKey() throws Exception {
        assertThat(this.restTemplate.getForObject(baseUrl() + "/jwks", String.class)).contains("\"kid\":\"xroad-multitenancy-test-service-jwt-key\"");
    }
}
