package fi.dvv.xroad.multitenancytestservicesoap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest()
class JwtServiceTest {

    @Autowired
    JwtService jwtService;

    @Test
    void generateJwtReturnsJwtWithCorrectSubject() throws Exception {
        String jwt = jwtService.generateJwt("FOO:12345-6", Date.from(Instant.now().plusSeconds(60*60*2)));
        assertThat(jwtService.validateJwt(jwt, "FOO:12345-6")).isTrue();

        String[] parts = jwt.split("\\.", 0);
        String decodedPayload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        assertThat(decodedPayload).contains("\"sub\":\"FOO:12345-6\"");
    }

    @Test
    void expiredJwtIsNotValidated() throws Exception {
        String jwt = jwtService.generateJwt("FOO:12345-6", Date.from(Instant.now().minusSeconds(60*60*2)));
        assertThat(jwtService.validateJwt(jwt, "FOO:12345-6")).isFalse();
    }

    @Test
    void invalidSubInJwtIsNotValidated() throws Exception {
        String jwt = jwtService.generateJwt("FOO:12345-6", Date.from(Instant.now().minusSeconds(60*60*2)));
        assertThat(jwtService.validateJwt(jwt, "BAR:45678-6")).isFalse();
    }

    @Test
    void getJwksPublicKeyReturnsJwks() throws Exception {
        String jwks = jwtService.getJwksPublicKey();
        assertThat(jwks).contains("\"kid\":\"xroad-multitenancy-test-service-soap-jwt-key\"");
        // assertThat(jwks).contains("\"alg\":\"RS256\"");
        assertThat(jwks).contains("\"kty\":\"RSA\"");
        // assertThat(jwks).contains("\"use\":\"sig\"");
        assertThat(jwks).contains("\"n\":\"");
        assertThat(jwks).contains("\"e\":\"");
    }
}
