package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionServiceRest;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionServiceSoap;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mock-xroad-connection-service")
@Configuration
public class XroadConnectionServiceMockConfiguration {
    @Bean
    @Primary
    public XroadConnectionServiceRest xroadConnectionServiceRest() {
        return Mockito.mock(XroadConnectionServiceRest.class);
    }

    @Bean
    @Primary
    public XroadConnectionServiceSoap xroadConnectionServiceSoap() {
        return Mockito.mock(XroadConnectionServiceSoap.class);
    }
}
