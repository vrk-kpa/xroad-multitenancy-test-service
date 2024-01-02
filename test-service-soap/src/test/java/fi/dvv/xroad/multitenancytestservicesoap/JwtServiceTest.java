/**
 * The MIT License
 * Copyright © 2018 Nordic Institute for Interoperability Solutions (NIIS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
