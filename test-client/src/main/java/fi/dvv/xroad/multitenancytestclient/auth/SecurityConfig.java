package fi.dvv.xroad.multitenancytestclient.auth;

import fi.dvv.xroad.multitenancytestclient.model.ConsumerServiceUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;



@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SubjectDnX509PrincipalExtractor principalExtractor = new SubjectDnX509PrincipalExtractor();
        principalExtractor.setSubjectDnRegex("CN=(.*?)(?:,|$)");

        UserDetailsService userDetailsService = new ConsumerServiceUserDetailsService();

        http.x509(x509 ->
                x509.x509PrincipalExtractor(principalExtractor)
                        .userDetailsService(userDetailsService)
        ).authorizeHttpRequests(authorize ->
                authorize.anyRequest().permitAll()
        );
        return http.build();
    }
}
