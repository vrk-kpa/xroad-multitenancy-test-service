openssl pkcs12 \
  -in ../test-client/keys/keystore.p12 \
  -clcerts \
  -nokeys \
  -password pass:changeit \
  -out trusted-certs/test-client-cert.pem
