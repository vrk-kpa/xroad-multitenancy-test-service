package fi.dvv.xroad.multitenancytestclient.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;



@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ConsumerServiceUserDetailsService userDetailsService;

    public SecurityConfig(ConsumerServiceUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SubjectDnX509PrincipalExtractor principalExtractor = new SubjectDnX509PrincipalExtractor();
        principalExtractor.setSubjectDnRegex("CN=(.*?)(?:,|$)");

        http.x509(x509 ->
                x509.x509PrincipalExtractor(principalExtractor)
                        .userDetailsService(userDetailsService)
        );

        return http.build();
    }
}
