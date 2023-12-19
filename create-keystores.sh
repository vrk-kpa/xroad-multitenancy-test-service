# Create keypair for test-service JWT signing
keytool -genkeypair \
    -alias xroad-multitenancy-test-service-jwt-key \
    -keyalg RSA \
    -keysize 4096 \
    -validity 365 \
    -dname "CN=xroad-multitenancy-test-service,OU=suomi-fi-palveluvayla,O=DVV,L=,S=CA,C=U" \
    -keypass changeit \
    -keystore test-service/keys/keystore.p12 \
    -storeType PKCS12 \
    -storepass changeit

# To enable TLS from security server to test-service, create a TLS certificate in test-service keystore
keytool -genkeypair \
    -alias xroad-multitenancy-test-service-tls-certificate \
    -keyalg RSA \
    -keysize 4096 \
    -validity 365 \
    -dname "CN=test-service,OU=suomi-fi-palveluvayla,O=DVV,L=,S=CA,C=U" \
    -keypass changeit \
    -keystore test-service/keys/keystore.p12 \
    -storeType PKCS12 \
    -storepass changeit

# Export the TLS certificate to pem format so it can be added to security server
keytool -export \
    -alias xroad-multitenancy-test-service-tls-certificate \
    -file test-service/keys/test-service-cert.pem \
    -keystore test-service/keys/keystore.p12 \
    -storepass changeit \
    -storeType PKCS12

# Create a keypair for test-service-soap JWT signing
keytool -genkeypair \
    -alias xroad-multitenancy-test-service-soap-jwt-key \
    -keyalg RSA \
    -keysize 4096 \
    -validity 365 \
    -dname "CN=xroad-multitenancy-test-service-soap,OU=suomi-fi-palveluvayla,O=DVV,L=,S=CA,C=U" \
    -keypass changeit \
    -keystore test-service-soap/keys/keystore.p12 \
    -storeType PKCS12 \
    -storepass changeit

# To enable TLS from security server to test-service-soap, create a TLS certificate in test-service-soap keystore
keytool -genkeypair \
    -alias xroad-multitenancy-test-service-soap-tls-certificate \
    -keyalg RSA \
    -keysize 4096 \
    -validity 365 \
    -dname "CN=test-service-soap,OU=suomi-fi-palveluvayla,O=DVV,L=,S=CA,C=U" \
    -keypass changeit \
    -keystore test-service-soap/keys/keystore.p12 \
    -storeType PKCS12 \
    -storepass changeit

# Export the TLS certificate to pem format so it can be added to security server
keytool -export \
    -alias xroad-multitenancy-test-service-soap-tls-certificate \
    -file test-service-soap/keys/test-service-soap-cert.pem \
    -keystore test-service-soap/keys/keystore.p12 \
    -storepass changeit \
    -storeType PKCS12

# Create certificate and private key for test-client TLS
keytool -genkeypair \
    -alias xroad-multitenancy-test-client \
    -keyalg RSA \
    -keysize 4096 \
    -validity 365 \
    -dname "CN=xroad-multitenancy-test-client,OU=suomi-fi-palveluvayla,O=DVV,L=,S=CA,C=U" \
    -keypass changeit \
    -keystore test-client/keys/keystore.p12 \
    -storeType PKCS12 \
    -storepass changeit

# Create certificates for external consumer organization mocks
(cd external-consumer && ./create-certs.sh)


# Import consumer organisation certificates to test-client trust-store
keytool \
    -import \
    -file external-consumer/org1/certs/cert.pem \
    -alias org1 \
    -keystore test-client/keys/truststore.p12 \
    -storepass changeit \
    -storeType PKCS12 \
    -noprompt

keytool \
    -import \
    -file external-consumer/org2/certs/cert.pem \
    -alias org2 \
    -keystore test-client/keys/truststore.p12 \
    -storepass changeit \
    -storeType PKCS12 \
    -noprompt

# External consumer mocks need to trust the test-client certificate for mTLS.
# Export test-client certificate to pem format for external consumer mock curls
(cd external-consumer && ./export-test-client-cert.sh)
