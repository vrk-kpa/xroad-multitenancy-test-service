package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.model.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "mock-xroad-connection-service"})
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

    @Autowired
    private XroadConnectionService xroadConnectionService;

    @Captor
    ArgumentCaptor<ConsumerServiceUser> principalCaptor;

    @Test
    void getRandomCallsXroad() throws Exception {
        assertThat(contextPath).isEqualTo("/rest-api");

        this.restTemplate.getForObject(baseUrl() + "/random", RandomNumberDto.class);

        Mockito.verify(
                xroadConnectionService,
                Mockito.times(1)
        ).getRandom(principalCaptor.capture());

        ConsumerServiceUser principal = principalCaptor.getValue();
        assertThat(principal.getXroadMemberClass()).isEqualTo("GOV");
        assertThat(principal.getXroadMemberCode()).isEqualTo("11111-1");
        assertThat(principal.getUsername()).isEqualTo("org1.com");
        assertThat(principal.getPasswordFromSecretsManager()).isEqualTo("password");
        assertThat(principal.getToken()).isNull();
    }

    @Test
    void getHelloCallsXroad() throws Exception {
        assertThat(contextPath).isEqualTo("/rest-api");

        this.restTemplate.getForObject(baseUrl() + "/hello", RandomNumberDto.class);

        Mockito.verify(
                xroadConnectionService,
                Mockito.times(1)
        ).getHello(principalCaptor.capture());

        ConsumerServiceUser principal = principalCaptor.getValue();
        assertThat(principal.getXroadMemberClass()).isEqualTo("GOV");
        assertThat(principal.getXroadMemberCode()).isEqualTo("11111-1");
        assertThat(principal.getUsername()).isEqualTo("org1.com");
        assertThat(principal.getPasswordFromSecretsManager()).isEqualTo("password");
        assertThat(principal.getToken()).isNull();
    }
}
