package fi.dvv.xroad.multitenancytestclient.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private RSAKey key;

    public JwtService() throws JOSEException, KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        char[] password = "changeit".toCharArray();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        File file = new File("keys/keys.p12");
        keyStore.load(new FileInputStream(file), password);
        key = RSAKey.load(keyStore, "xroad-multi-tenancy-test", password);
    }

    public String generateJwt(String subject, Date expirationTime) throws JOSEException {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(key.getKeyID())
                .build();

        JWTClaimsSet payload = new JWTClaimsSet.Builder()
                .issuer("xroad-multi-tenancy-test-service")
                .audience("xroad")
                .subject(subject)
                .expirationTime(expirationTime)
                .build();

        SignedJWT signedJWT = new SignedJWT(header, payload);
        signedJWT.sign(new RSASSASigner(key.toRSAPrivateKey()));
        String jwt = signedJWT.serialize();
        return jwt;
    }

    public boolean validateJwt(String jwt) throws ParseException, JOSEException {
        try {
            SignedJWT parsedJwt = SignedJWT.parse(jwt);
            boolean signatureOk = parsedJwt.verify(new RSASSAVerifier(key.toRSAPublicKey()));
            JWTClaimsSet claims = parsedJwt.getJWTClaimsSet();

            boolean expirationOk = claims.getExpirationTime().after(Date.from(Instant.now()));
            boolean issuerOk = claims.getIssuer().equals("xroad-multi-tenancy-test-service");
            boolean audienceOk = claims.getAudience().contains("xroad");
            boolean subjectOk = !claims.getSubject().isEmpty();
            return signatureOk && expirationOk && issuerOk && audienceOk && subjectOk;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubject(String jwt) throws ParseException, JOSEException {
        JWTClaimsSet claims = SignedJWT.parse(jwt).getJWTClaimsSet();
        return claims.getSubject();
    }

    public String getJwksPublicKey() {
        return key.toPublicJWK().toJSONString();
    }
}
