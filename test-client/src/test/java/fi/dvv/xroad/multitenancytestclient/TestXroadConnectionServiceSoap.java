package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.auth.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionServiceSoap;
import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "mockserver2"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestXroadConnectionServiceSoap {

    @Autowired
    XroadConnectionServiceSoap service;

    static private ClientAndServer mockServer;

    @Value("${security-server.service-id}")
    private String serviceId;

    @Value("${security-server.client-id}")
    private String clientId;

    private XroadMockServerSoapTransactions transactions;

    @BeforeAll
    public static void startServer() {
        mockServer = startClientAndServer(8282);
    }

    @BeforeEach
    public void resetServer() {
        transactions = new XroadMockServerSoapTransactions(clientId, serviceId);
    }

    @AfterEach
    public void resetMocks() {
        mockServer.reset();
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }

    @Test
    @Order(1)
    public void callingGetRandomWithTokenReturnsRandomNumber() {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");
        principal.setToken(service.TOKEN_ID, "goodtoken");

        HttpRequest randomRequest = transactions.getRandomRequest();
        mockServer.when(randomRequest, exactly(1)).respond(transactions.getRandomResponse());

        RandomNumberDto response = service.makeGetRandomRequest(principal);

        assertThat(mockServer.retrieveRecordedRequests(randomRequest)).hasSize(1);

        assertThat(response.data()).isEqualTo(42); // the mock value
    }

    @Test
    @Order(2)
    public void callingGetRandomWithoutTokenTriggersLogin() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");

        HttpRequest loginRequest = transactions.getAuthenticateRequest();
        mockServer.when(loginRequest, exactly(1)).respond(transactions.getAuthenticateResponse());

        HttpRequest randomRequest = transactions.getRandomRequest();
        mockServer.when(randomRequest, exactly(1)).respond(transactions.getRandomResponse());

        RandomNumberDto response = service.makeGetRandomRequest(principal);

        assertThat(mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(randomRequest)).hasSize(1);

        assertThat(response.data()).isEqualTo(42); // the mock value
    }

    @Test
    @Order(3)
    public void callingGetRandomWithInvalidTokenTriggersLogin() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");
        principal.setToken(service.TOKEN_ID, "badtoken");

        HttpRequest failingRandomRequest = transactions.getRandomWithInvalidTokenRequest();
        mockServer.when(failingRandomRequest, exactly(1)).respond(transactions.getUnauthorizedResponse());

        HttpRequest loginRequest = transactions.getAuthenticateRequest();
        mockServer.when(loginRequest, exactly(1)).respond(transactions.getAuthenticateResponse());

        HttpRequest succeedingRandomRequest = transactions.getRandomRequest();
        mockServer.when(succeedingRandomRequest, exactly(1)).respond(transactions.getRandomResponse());

        RandomNumberDto response = service.makeGetRandomRequest(principal);

        assertThat(mockServer.retrieveRecordedRequests(failingRandomRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(succeedingRandomRequest)).hasSize(1);

        assertThat(response.data()).isEqualTo(42); // the mock value
    }



    @Test
    @Order(4)
    public void callingGetHelloWithTokenReturnsGreeting() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");
        principal.setToken(service.TOKEN_ID, "goodtoken");

        HttpRequest helloRequest = transactions.getHelloRequest();
        mockServer.when(helloRequest, exactly(1)).respond(transactions.getHelloResponse());

        MessageDto response = service.makeHelloServiceRequest(principal, "baz");

        assertThat(mockServer.retrieveRecordedRequests(helloRequest)).hasSize(1);
        assertThat(response.message()).isEqualTo("Hello"); // the mock value
    }

    @Test
    @Order(5)
    public void callingGetHelloWithoutTokenTriggersLogin() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");

        HttpRequest loginRequest = transactions.getAuthenticateRequest();
        mockServer.when(loginRequest, exactly(1)).respond(transactions.getAuthenticateResponse());

        HttpRequest helloRequest = transactions.getHelloRequest();
        mockServer.when(helloRequest, exactly(1)).respond(transactions.getHelloResponse());

        MessageDto response = service.makeHelloServiceRequest(principal, "baz");

        assertThat(mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(helloRequest)).hasSize(1);

        assertThat(response.message()).isEqualTo("Hello"); // the mock value
    }

    @Test
    @Order(6)
    public void callingGetHelloWithInvalidTokenTriggersLogin() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");
        principal.setToken(service.TOKEN_ID, "badtoken");

        HttpRequest failingHelloRequest = transactions.getHelloWithInvalidTokenRequest();
        mockServer.when(failingHelloRequest, exactly(1)).respond(transactions.getUnauthorizedResponse());

        HttpRequest loginRequest = transactions.getAuthenticateRequest();
        mockServer.when(loginRequest, exactly(1)).respond(transactions.getAuthenticateResponse());

        HttpRequest succeedingHelloRequest = transactions.getHelloRequest();
        mockServer.when(succeedingHelloRequest, exactly(1)).respond(transactions.getHelloResponse());

        MessageDto response = service.makeHelloServiceRequest(principal, "baz");

        assertThat(mockServer.retrieveRecordedRequests(failingHelloRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(succeedingHelloRequest)).hasSize(1);

        assertThat(response.message()).isEqualTo("Hello"); // the mock value
    }
}
