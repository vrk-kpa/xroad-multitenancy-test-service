package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionService;
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
    public XroadConnectionService xroadConnectionService() {
        return Mockito.mock(XroadConnectionService.class);
    }
}
