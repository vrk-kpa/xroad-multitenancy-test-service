# X-Road Multitenancy Reference Implementation

## Overview
The services in this repository are intended to provide a reference implementation of the Suomi.fi Palveluväylä multi-tenancy 
features documented in [Suomi.fi Palveluhallinta](https://palveluhallinta.suomi.fi/fi/tuki/artikkelit/63f8ab85e0763400245f2c8f).

The purpose of the multi-tenancy solution is to allow multiple independent consumer organisations to share a single X-Road
subsystem and security server provided by a 3rd party multi-tenancy operator.

This reference implementation consists of the followin modules:
 * [test-client](test-client/README.md): the multi-tenancy operator's information system that relays messages from external consumer organisations to X-Road services
 * [test-service](test-service/README.md): a REST service that can serve multi-tenant clients over X-Road
 * [test-service-soap](test-service-soap/README.md): a SOAP service that can serve multi-tenant clients over X-Road
 * [external-consumer](external-consumer/README.md): scripts to mock consumer organisations that make calls to X-Road 
   services through multi-tenancy operator's system.


The services in this repository are not production ready, but are intended as a starting point for developing
multi-tenant X-Road services. Please refer to service specific README files for more information about each service.


## Dependencies
 * Java 17 for test-client and test-service
 * Java 11 for test-service-soap
 * jenv or similar to manage multiple Java versions
 * Maven
 * Docker

## Running locally with docker compose

### Create certificates and trust stores

Setting up this application requires that some certificates are created and added to trust stores.
Detailed explanation of the required certificates is included later in this document.
For a quick-start, you can create required certificates and trust stores by running the script:
```shell
./create-keystores.sh
```

### Build and run the containers using docker compose

Then build the application JAR files and start the services in docker compose:
```shell
cd test-client
jenv local 17.0
mvn clean package

cd ../test-service
jenv local 17.0
mvn clean package

cd ../test-service-soap
jenv local 11.0
mvn clean package

cd ..
docker compose build
docker compose up -d
```

It takes a while for the security server to start up. You can check the logs with `docker compose logs -f ss`.

### Register test-service to the security server
Login to the security server UI by opening https://localhost:4000 in your browser. 
The username is `xrd` and the password is `secret`.

Standalone security server comes predefined with an X-Road service and a client, 
named `CS:ORG:1111:TestService` and `CS:ORG:1111:TestClient` respectively.
Add the test-service to the service list of `CS:ORG:1111:TestService` as a REST service.
by providing the service's OpenApi description URL: `https://test-service:8443/multitenancy-rest-test-service/api/api-docs`. 
Security server will then automatically add all the service endpoints when creating the X-Road service. 
Set the service-code to `rest-test` and remember to enable the service.

In `CS:ORG:1111:TestService`'s service client settings, add a perimission for subject `CS:ORG:1111:TestClient` 
to make calls to the service code `rest-test`.

Next add the test-service certificate to the security server's trust list. Open the tab 'Internal Servers' and in 
section 'Information System TLS certificate' click the 'Add' button. Select the file `test-service/keys/test-service-cert.pem` that
was created by the `create-certs.sh` script above.

### Register test-service-soap to the security server
Add the test-service-soap to the service list of `CS:ORG:1111:TestService` as a SOAP service by providing the
service's WSDL URL: `https://test-service-soap:8443/multitenancy-soap-test-service/Endpoint?wsdl`.

This should add new services with service code `authenticate`, `getRandom` and `helloService` to the service list.
Enable all the new services and add permissions for `CS:ORG:1111:TestClient` to call them.

Then add the test-service-soap certificate to the security server's trust list as described above. 
The certificate file to add is `test-service-soap/keys/test-service-soap-cert.pem`.

For more info about and examples of standalone security server configuration, refer to 
[the tutorial](https://github.com/digitaliceland/Straumurinn/blob/master/DOC/Manuals/standalone_security_server_tutorial.md).

### Setup TLS from test-client to security server
Download the security server's certificate from the security server UI. Navigate to 
`Keys and certificates -> Security server TLS key` and click the `Export cert` button.
Open the certificate details by clicking the hash value in security server UI and check the CN name.
Check that security server hostname in test-client's `application.yaml` configuration and the alias name 
set for the security server in `docker-compose yaml` both match to this CN name.

Unpack the downloaded `certs.tar.gz` in some temporary folder and add the `cert.pem` to test-client trust store:
```shell
```shell
tar -xzf certs.tar.gz

keytool \
  -importcert \
  -alias ss \
  -file cert.pem \
  -keystore {project-root}/test-client/keys/truststore.p12 \
  -storetype pkcs12 \
  -storepass changeit \
  -noprompt
```

Now you can set the security server to accept only requests over HTTPS. 
Navigate to `Internal servers` tab and set connection type as `HTTPS NO AUTH`.

The other option named only `HTTPS` requires that also the test-client's certificate is added 
to the security servers trust list (mTLS). This could be done for added security in a zero-trust network and is left as
an exercise for the reader.

After adding the certificate to test-client's trust store, you need to restart the test-client to reload the trust-store file:
```shell
docker compose restart test-client
```


### Try it out
The folder ``external-consumer`` contains a script to make calls to the test-service as a mock organisation.
There are three mock organisations, `org1`, `org2` and `org3`, and the `create-certs.sh` script 
created certificates for them. The certificates of org1 and org2 are added to the test-client's trust store.
Certificate of org3 is not trusted and can be used to test that calls from untrusted consumer will fail.

When testing locally you must add the following line to `/etc/hosts` to match the test-clients certificate CN:
```
127.0.0.1 xroad-multitenancy-test-client
```

The `test-request.sh` script uses curl to make calls to the test-client using the mock organisations' certificates.

To request a hello-greeting from test-service as `org1`, run:
```shell
cd external-consumer
./test-request.sh org1 "hello?name=John"
```

To request a hello-greeting from test-service-soap as `org2`, run:
```shell
./test-request.sh org2 "hello?name=John&protocol=soap"
```

`org3` certificate is not trusted by test-client, so it can be used to test certificate validation error:
```shell
./test-request.sh org3 "hello?name=John"
```
