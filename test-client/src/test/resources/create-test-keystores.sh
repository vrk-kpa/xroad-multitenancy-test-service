# Create a keystore with cert and private key of external consumer org1:
keytool -genkeypair \
    -alias org1 \
    -keyalg RSA \
    -keysize 4096 \
    -validity 3650 \
    -dname "CN=org1.com,OU=foo,O=bar,L=,S=CA,C=U" \
    -keypass changeit \
    -keystore external-consumer-keystore.p12 \
    -storeType PKCS12 \
    -storepass changeit


# Create a keystore with cert and private key of test-client:
keytool -genkeypair \
    -alias xroad-multitenancy-test-client \
    -keyalg RSA \
    -keysize 4096 \
    -validity 3650 \
    -dname "CN=localhost,OU=foo,O=bar,L=,S=CA,C=U" \
    -keypass changeit \
    -keystore multitenancy-client-keystore.p12 \
    -storeType PKCS12 \
    -storepass changeit


# Export the consumer cert from the keystore:
openssl pkcs12 \
  -in external-consumer-keystore.p12 \
  -clcerts \
  -nokeys \
  -password pass:changeit \
  -out external-consumer-cert.pem


# Create a unit test truststore for the test-client and add the consumer cert to it:
keytool \
    -import \
    -file external-consumer-cert.pem \
    -alias org1 \
    -keystore multitenancy-client-truststore.p12 \
    -storepass changeit \
    -storeType PKCS12 \
    -noprompt

# Remove the intermediate .pem file:
rm external-consumer-cert.pem

