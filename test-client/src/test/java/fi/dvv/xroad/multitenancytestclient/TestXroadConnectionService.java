package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.model.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestXroadConnectionService {

    @Autowired
    XroadConnectionService service;

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
    public void callingGetRandomWithTokenReturnsRandomNumber() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");
        principal.setToken("Bearer foo");

        HttpRequest randomRequest = transactions.getRandomRequest();
        mockServer.when(randomRequest, exactly(1)).respond(transactions.getRandomResponse());

        RandomNumberDto response = service.getRandom(principal);

        assertThat(mockServer.retrieveRecordedRequests(randomRequest)).hasSize(1);

        assertThat(response.data()).isEqualTo(42); // the mock value
    }

    @Test
    public void callingGetRandomWithoutTokenTriggersLogin() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");

        HttpRequest loginRequest = transactions.getLoginRequest();
        mockServer.when(loginRequest, exactly(1)).respond(transactions.getLoginResponse());

        HttpRequest randomRequest = transactions.getRandomRequest();
        mockServer.when(randomRequest, exactly(1)).respond(transactions.getRandomResponse());

        RandomNumberDto response = service.getRandom(principal);

        assertThat(mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(randomRequest)).hasSize(1);

        assertThat(response.data()).isEqualTo(42); // the mock value
    }

    @Test
    public void callingGetRandomWithInvalidTokenTriggersLogin() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");
        principal.setToken("Bearer bar");

        HttpRequest failingRandomRequest = transactions.getRandomWithInvalidTokenRequest();
        mockServer.when(failingRandomRequest, exactly(1)).respond(transactions.getUnauthorizedResponse());

        HttpRequest loginRequest = transactions.getLoginRequest();
        mockServer.when(loginRequest, exactly(1)).respond(transactions.getLoginResponse());

        HttpRequest succeedingRandomRequest = transactions.getRandomRequest();
        mockServer.when(succeedingRandomRequest, exactly(1)).respond(transactions.getRandomResponse());

        RandomNumberDto response = service.getRandom(principal);

        assertThat(mockServer.retrieveRecordedRequests(failingRandomRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(succeedingRandomRequest)).hasSize(1);

        assertThat(response.data()).isEqualTo(42); // the mock value
    }



    @Test
    public void callingGetHelloWithTokenReturnsGreeting() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");
        principal.setToken("Bearer foo");

        HttpRequest helloRequest = transactions.getHelloRequest();
        mockServer.when(helloRequest, exactly(1)).respond(transactions.getHelloResponse());

        MessageDto response = service.getHello(principal, "foo");

        assertThat(mockServer.retrieveRecordedRequests(helloRequest)).hasSize(1);
        assertThat(response.message()).isEqualTo("Hello"); // the mock value
    }

    @Test
    public void callingGetHelloWithoutTokenTriggersLogin() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");

        HttpRequest loginRequest = transactions.getLoginRequest();
        mockServer.when(loginRequest, exactly(1)).respond(transactions.getLoginResponse());

        HttpRequest helloRequest = transactions.getHelloRequest();
        mockServer.when(helloRequest, exactly(1)).respond(transactions.getHelloResponse());

        MessageDto response = service.getHello(principal, "foo");

        assertThat(mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(helloRequest)).hasSize(1);

        assertThat(response.message()).isEqualTo("Hello"); // the mock value
    }

    @Test
    public void callingGetHelloWithInvalidTokenTriggersLogin() throws Exception {
        ConsumerServiceUser principal = new ConsumerServiceUser("org1.com", "password", "GOV", "11111-1");
        principal.setToken("Bearer bar");

        HttpRequest failingHelloRequest = transactions.getHelloWithInvalidTokenRequest();
        mockServer.when(failingHelloRequest, exactly(1)).respond(transactions.getUnauthorizedResponse());

        HttpRequest loginRequest = transactions.getLoginRequest();
        mockServer.when(loginRequest, exactly(1)).respond(transactions.getLoginResponse());

        HttpRequest succeedingHelloRequest = transactions.getHelloRequest();
        mockServer.when(succeedingHelloRequest, exactly(1)).respond(transactions.getHelloResponse());

        MessageDto response = service.getHello(principal, "foo");

        assertThat(mockServer.retrieveRecordedRequests(failingHelloRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(loginRequest)).hasSize(1);
        assertThat(mockServer.retrieveRecordedRequests(succeedingHelloRequest)).hasSize(1);

        assertThat(response.message()).isEqualTo("Hello"); // the mock value
    }
}
