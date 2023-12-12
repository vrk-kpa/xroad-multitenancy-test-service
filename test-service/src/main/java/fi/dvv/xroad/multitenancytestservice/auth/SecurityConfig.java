package fi.dvv.xroad.multitenancytestservice.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    final private JwtAuthorizationFilter jwtAuthorizationFilter;
    final private DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthorizationFilter jwtAuthorizationFilter, DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.delegatedAuthenticationEntryPoint = delegatedAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                //.requestMatchers("/jwks", "/login", "/random", "/message").permitAll()
                                .requestMatchers("/private/**").authenticated()
                                .anyRequest().permitAll()

                )
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling.authenticationEntryPoint(delegatedAuthenticationEntryPoint))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }
}
