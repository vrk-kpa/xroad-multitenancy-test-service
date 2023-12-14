# xroad-multitenancey-test-service

A test service to demonstrate X-Road multitenancy with REST services.

## Dependencies
 * Java 17
 * Maven
 * Docker for local development

## Local development with standalone security server

Build the application JAR file and start the app and security server in docker compose:
```shell
mvn clean package
cd local-dev
docker-compose build
docker-compose up
```

It takes a while for the security server to start up. You can check the logs with `docker compose logs -f ss`.

Login to the security server UI by opening http://localhost:4000 in your browser. 
The username is `xrd` and the password is `secret`.

Standalone security server has one X-Road service and one client, 
named `CS:ORG:1111:TestService` and `CS:ORG:1111:TestClient` respectively.
Add the rest-test-service to the service list of `CS:ORG:1111:TestService` as a REST service.
Service base URL in docker network is `http://test-service:8080`. When adding the service you can provide the service's
OpenApi description URL: `http://test-service:8080/rest-api/api-docs`. Security server will then automatically add all 
the service endpoints when creating the X-Road service. Set the service-code to `rest-test` and remember to enable the 
service.

In `CS:ORG:1111:TestService` service client settings, add a perimission for subject `CS:ORG:1111:TestClient` 
to make calls to the service code `rest-test`.
For detailed example of standalone security server configuration, refer to 
[the tutorial](https://github.com/digitaliceland/Straumurinn/blob/master/DOC/Manuals/standalone_security_server_tutorial.md).

To call the service with curl, first send a login command:
```shell
curl -v \
  -X GET \
  -H 'X-Road-Client: CS/ORG/1111/TestClient' \
  -H "X-Road-Represented-Party: Foo/BAR" \
  -H "Member-Username: Foo/BAR" \
  -H "Member-Password: password" \
  -i 'http://localhost:8080/r1/CS/ORG/1111/TestService/multitenancy-test/login'
```

In the response headers you get the JWT token that can be used to authenticate when calling the private endpoints:
```shell
curl -v \
  -X GET \
  -H 'X-Road-Client: CS/ORG/1111/TestClient' \
  -H "Authorization: Bearer <your-token-here>" \
  -i 'http://localhost:8080/r1/CS/ORG/1111/TestService/multitenancy-test/private/greeting'
  
```

## Generating keys and certificates
### test-service
Needs a key-pair to create JWTs.

### test-client
Needs a certificate for SSL:
```
keytool -genkeypair \
    -alias xroad-multitenancy-test-client \
    -keyalg RSA \
    -keysize 4096 \
    -validity 3650 \
    -dname "CN=xroad-multitenancy-test-client,OU=suomi-fi-palveluvayla,O=DVV,L=,S=CA,C=U" \
    -keypass changeit \
    -keystore test-client/keys/keystore.p12 \
    -storeType PKCS12 \
    -storepass changeit
```

### mocking external consumer organisations
To mock external consumers, you need to create certificates for them.
`external-consumer` directory contains a script to generate certificates 
for two mock organisations, `org1`and `org2`:
```shell
cd external-consumer
./generate-certs.sh
```


## Adding certificates to trust stores
### test-service

### test-client
Add the certs of external consumer organisations org1 and org2 to the client's trust store.
Cert of org3 is not added to allow testing that calls from untrusted consumer will fail.

```
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
```

### mocking external consumer organisations
Externals consumers need to trust the test-clients certificate.
To export the test-client certificate from test-client keystore in pem format, run:
```shell
cd external-consumer
./export-test-client-cert.sh
```

The `test-request.sh` script then uses the exported certificate to trust the test-client 
when making calls as a mock organisation.

When testing locally you must add the following line to `/etc/hosts` to match the test-clients certificate CN:
```
127.0.0.1 xroad-multitenancy-test-client
```
