package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.matchers.Times.unlimited;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiTest {

    @LocalServerPort
    private int port;

    @Value("${server.servlet.contextPath}")
    private String contextPath;

    private String baseUrl() {
        return "https://localhost:" + port + contextPath;
    };

    // This autowires to the RestTemplate created in TestRestTemplateClient.
    // It is configured to use the external-consumer-keystore.p12
    @Autowired
    private RestTemplate restTemplate;

    private ClientAndServer mockServer;

    @Value("${security-server.service-id}")
    private String serviceId;

    @BeforeEach
    public void startServer() {
        mockServer = startClientAndServer(8181);
    }

    @AfterEach
    public void stopServer() {
        mockServer.stop();
    }


    @Test
    void randomGetsTokenAndRandomNumberThroughSecurityServer() throws Exception {
        assertThat(contextPath).isEqualTo("/rest-api");

        HttpRequest randomRequest = request().withMethod("GET").withPath("/r1/" + serviceId + "/private/random")
                .withHeader("Authorization", "Bearer foo");

        mockServer.when(randomRequest, exactly(2))
                .respond(response().withStatusCode(200)
                                .withHeader("content-type", "application/json")
                                .withBody("{\"data\": 42}"));

        HttpRequest loginRequest = request().withMethod("GET").withPath("/r1/" + serviceId + "/login");
        mockServer.when(loginRequest, exactly(1))
                .respond(response().withStatusCode(200)
                                .withHeader("Authorization", "Bearer foo")
                                .withHeader("content-type", "application/json")
                                .withBody("{\"message\": \"Login success\"}"));


        // On first call to /random, the client will call /login to get a token.
        assertThat(this.restTemplate.getForObject(baseUrl() + "/random", RandomNumberDto.class).data()).isEqualTo(42);

        // On second call the login is not called again
        assertThat(this.restTemplate.getForObject(baseUrl() + "/random", RandomNumberDto.class).data()).isEqualTo(42);

        assertThat(this.mockServer.retrieveRecordedRequests(randomRequest)).hasSize(2);
        assertThat(this.mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
    }

    @Test
    void helloGetsTokenAndGreetingThroughSecurityServer() throws Exception {
        assertThat(contextPath).isEqualTo("/rest-api");

        HttpRequest helloRequest = request().withMethod("GET").withPath("/r1/" + serviceId + "/private/hello")
                .withHeader("Authorization", "Bearer foo");
        mockServer.when(helloRequest, exactly(2))
                .respond(response().withStatusCode(200)
                                .withHeader("content-type", "application/json")
                                .withBody("{\"message\": \"Hello\"}"));


        HttpRequest loginRequest = request().withMethod("GET").withPath("/r1/" + serviceId + "/login");
        mockServer.when(loginRequest, exactly(1))
                .respond(response().withStatusCode(200)
                                .withHeader("Authorization", "Bearer foo")
                                .withHeader("content-type", "application/json")
                                .withBody("{\"message\": \"Login success\"}"));


        assertThat(this.restTemplate.getForObject(baseUrl() + "/hello", MessageDto.class).message()).isEqualTo("Hello");
        assertThat(this.restTemplate.getForObject(baseUrl() + "/hello", MessageDto.class).message()).isEqualTo("Hello");

        assertThat(this.mockServer.retrieveRecordedRequests(helloRequest)).hasSize(2);
        assertThat(this.mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
    }

    /*
    @Test
    void greetingReturnsGreetingMessage() throws Exception {
        assertThat(this.restTemplate.getForObject(baseUrl() + "/hello", MessageDto.class).message()).isEqualTo("Hello! Greetings from adapter server!");
    }

    @Test
    void greetingReturnsGreetingMessageWithName() throws Exception {
        assertThat(this.restTemplate.getForObject(new URI(baseUrl() + "/hello?name=Gandalf"), MessageDto.class).message()).isEqualTo("Hello Gandalf! Greetings from adapter server!");
    }

    @Test
    void greetingReturnsGreetingMessageWithComplexName() throws Exception {
        assertThat(this.restTemplate.getForObject(new URI(baseUrl() + "/hello?name=X%20%C3%86%20A-12"), MessageDto.class).message()).isEqualTo("Hello X Æ A-12! Greetings from adapter server!");
    }

    @Test
    void greetingReturnsGreetingMessageWithNameEscapedForJson() throws Exception {
        assertThat(this.restTemplate.getForObject(baseUrl() + "/hello?name=<script>alert('Executed!');</script>", MessageDto.class).message()).isEqualTo("Hello <script>alert('Executed!');<\\/script>! Greetings from adapter server!");
    }

    @Test
    void greetingReturnsErrorForTooLongName() throws Exception {
        String name = "a".repeat(257);
        ErrorDto error = this.restTemplate.getForObject(baseUrl() + "/hello?name=" + name, ErrorDto.class);
        assertThat(error.errorMessage()).isEqualTo("Name is too long. Max length is 256 characters.");
        assertThat(error.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void nonExistentEndpointReturnsError() throws Exception {
        ErrorDto error = this.restTemplate.getForObject(baseUrl() + "/not-here", ErrorDto.class);
        assertThat(error.errorMessage()).isEqualTo("No endpoint GET /rest-api/not-here.");
        assertThat(error.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void wrongMethodReturnsError() throws Exception {
        ErrorDto error = this.restTemplate.postForObject(baseUrl() + "/random", "foo", ErrorDto.class);
        assertThat(error.errorMessage()).isEqualTo("Request method 'POST' is not supported");
        assertThat(error.httpStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    */
}
