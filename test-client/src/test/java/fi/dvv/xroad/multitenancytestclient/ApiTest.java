package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;

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

    private XroadMockServerTransactions transactions;

    @BeforeEach
    public void startServer() {
        transactions = new XroadMockServerTransactions(serviceId);
        mockServer = startClientAndServer(8181);
    }

    @AfterEach
    public void stopServer() {
        mockServer.stop();
    }

    @Test
    void randomGetsTokenAndRandomNumberThroughSecurityServer() throws Exception {
        assertThat(contextPath).isEqualTo("/rest-api");

        HttpRequest randomRequest = transactions.getRandomRequest();
        mockServer.when(randomRequest, exactly(2))
                .respond(transactions.getRandomResponse());

        HttpRequest loginRequest = transactions.getLoginRequest();
        mockServer.when(loginRequest, exactly(1))
                .respond(transactions.getLoginResponse());

        // Call /random twice. First time /login is called if token is not cached. Second call uses cached token.
        assertThat(this.restTemplate.getForObject(baseUrl() + "/random", RandomNumberDto.class).data()).isEqualTo(42);
        assertThat(this.restTemplate.getForObject(baseUrl() + "/random", RandomNumberDto.class).data()).isEqualTo(42);

        assertThat(this.mockServer.retrieveRecordedRequests(randomRequest)).hasSize(2);

        // The login is called 0 or 1 times, depending on whether the token was already cached.
        assertThat(this.mockServer.retrieveRecordedRequests(loginRequest)).hasSizeLessThan(2);
    }

    @Test
    void helloGetsTokenAndGreetingThroughSecurityServer() throws Exception {
        assertThat(contextPath).isEqualTo("/rest-api");

        HttpRequest helloRequest = transactions.getHelloRequest();
        mockServer.when(helloRequest, exactly(2))
                .respond(transactions.getHelloResponse());


        HttpRequest loginRequest = transactions.getLoginRequest();
        mockServer.when(loginRequest, exactly(1))
                .respond(transactions.getLoginResponse());


        // Call /hello twice. First time /login is called if token is not cached. Second call uses cached token.
        assertThat(this.restTemplate.getForObject(baseUrl() + "/hello", MessageDto.class).message()).isEqualTo("Hello");
        assertThat(this.restTemplate.getForObject(baseUrl() + "/hello", MessageDto.class).message()).isEqualTo("Hello");

        assertThat(this.mockServer.retrieveRecordedRequests(helloRequest)).hasSize(2);

        // The login is called 0 or 1 times, depending on whether the token was already cached.
        assertThat(this.mockServer.retrieveRecordedRequests(loginRequest)).hasSizeLessThan(2);
    }
}
