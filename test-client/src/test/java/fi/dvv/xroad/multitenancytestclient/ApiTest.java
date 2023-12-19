package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.model.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionServiceRest;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionServiceSoap;
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
    }

    // This autowires to the RestTemplate created in TestRestTemplateClient.
    // It is configured to use the external-consumer-keystore.p12
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private XroadConnectionServiceRest xroadConnectionServiceRest;

    @Autowired
    private XroadConnectionServiceSoap xroadConnectionServiceSoap;

    @Captor
    ArgumentCaptor<ConsumerServiceUser> principalCaptor;

    @Test
    void getRandomCallsXroadRest() {
        assertThat(contextPath).isEqualTo("/rest-api");

        this.restTemplate.getForObject(baseUrl() + "/random", RandomNumberDto.class);

        Mockito.verify(
                xroadConnectionServiceRest,
                Mockito.times(1)
        ).getRandom(principalCaptor.capture());

        ConsumerServiceUser principal = principalCaptor.getValue();
        assertThat(principal.getXroadMemberClass()).isEqualTo("GOV");
        assertThat(principal.getXroadMemberCode()).isEqualTo("11111-1");
        assertThat(principal.getUsername()).isEqualTo("org1.com");
        assertThat(principal.getPasswordFromSecretsManager()).isEqualTo("password");
        assertThat(principal.getToken(xroadConnectionServiceRest.TOKEN_ID)).isNull();
    }

    @Test
    void getHelloCallsXroadRest() {
        assertThat(contextPath).isEqualTo("/rest-api");

        this.restTemplate.getForObject(baseUrl() + "/hello", RandomNumberDto.class);

        Mockito.verify(
                xroadConnectionServiceRest,
                Mockito.times(1)
        ).getHello(principalCaptor.capture(), Mockito.any());

        ConsumerServiceUser principal = principalCaptor.getValue();
        assertThat(principal.getXroadMemberClass()).isEqualTo("GOV");
        assertThat(principal.getXroadMemberCode()).isEqualTo("11111-1");
        assertThat(principal.getUsername()).isEqualTo("org1.com");
        assertThat(principal.getPasswordFromSecretsManager()).isEqualTo("password");
        assertThat(principal.getToken(xroadConnectionServiceRest.TOKEN_ID)).isNull();
    }

    @Test
    void getRandomWithProtocolSoapCallsXroadSoap() {
        assertThat(contextPath).isEqualTo("/rest-api");

        this.restTemplate.getForObject(baseUrl() + "/random?protocol=soap", RandomNumberDto.class);

        Mockito.verify(
                xroadConnectionServiceSoap,
                Mockito.times(1)
        ).makeGetRandomRequest(principalCaptor.capture());

        ConsumerServiceUser principal = principalCaptor.getValue();
        assertThat(principal.getXroadMemberClass()).isEqualTo("GOV");
        assertThat(principal.getXroadMemberCode()).isEqualTo("11111-1");
        assertThat(principal.getUsername()).isEqualTo("org1.com");
        assertThat(principal.getPasswordFromSecretsManager()).isEqualTo("password");
        assertThat(principal.getToken(xroadConnectionServiceSoap.TOKEN_ID)).isNull();
    }

    @Test
    void getHelloWithProtocolSoapCallsXroadSoap() {
        assertThat(contextPath).isEqualTo("/rest-api");

        this.restTemplate.getForObject(baseUrl() + "/hello?protocol=soap&name=foo", RandomNumberDto.class);

        Mockito.verify(
                xroadConnectionServiceSoap,
                Mockito.times(1)
        ).makeHelloServiceRequest(principalCaptor.capture(), Mockito.any());

        ConsumerServiceUser principal = principalCaptor.getValue();
        assertThat(principal.getXroadMemberClass()).isEqualTo("GOV");
        assertThat(principal.getXroadMemberCode()).isEqualTo("11111-1");
        assertThat(principal.getUsername()).isEqualTo("org1.com");
        assertThat(principal.getPasswordFromSecretsManager()).isEqualTo("password");
        assertThat(principal.getToken(xroadConnectionServiceSoap.TOKEN_ID)).isNull();
    }
}
