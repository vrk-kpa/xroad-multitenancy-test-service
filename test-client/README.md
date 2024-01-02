# Multi-tenant test-client

## Overview

The test-client is a multi-tenancy operator's information system that relays messages from external consumer organisations to X-Road services.
In the X-Road multitenancy setup, the multitenancy operator is responsible for the following tasks:
* Identifying the consumer organisations using mTLS
* Providing an API that the consumer organisations can use to access the X-Road services
* Mapping the consumer organisations TLS certificates to their X-Road client information
* Managing the consumer organisations X-Road service login credentials
* Exchanging the login credentials to JWT tokens of the X-Road services
* Managing the consumer organisation JWTs
* Making requests to the X-Road services through the operator's X-Road security server
* Adding the consumer organisation's X-Road client information and JWT to the X-Road requests

The test-client provides a reference implementation of the above tasks in a Docker compose environment using
NIIS provided stand-alone security server and the test-services in this repository.

## Dependencies
* Java 17
* Maven
* Docker

## What is implemented
The test-client is a proof-of-concept implementation on how to use the X-Road Third Party Representation Extension and
Security Token Extension to pass the consumer organisation's identity to X-Road services.

The test-client provides a simple REST API that the example consumer organisations defined in ``external-consumer`` folder can
use to call both the SOAP and REST based test services in this repo. The API has two endpoints:

* `GET /hello`: calls the test-service to get a random number with query parameters:
  * `protocol` specifies the protocol of the target test-service. By default, REST service is called. Use `protocol=soap` to call the SOAP service.
* `GET /random`: calls the test-service to get a greeting with query parameters: 
  * `name` specifies the custom name to be used in the greeting
  * `protocol` similar to `/hello` endpoint


For example when test organisation ``org1`` wants to call the HelloService SOAP service, it can call the test-client
endpoint ``/hello?protocol=soap&name=Mary``. The test-client will identify the organisation by its TLS certificate, 
get a JWT token if needed and make a SOAP request to `test-service-soap` over X-Road to get the greeting. 
More detailed examples of calling the test-client API can be found in the scripts in ``external-consumer`` folder.


## What is not implemented
The test-client implementation is not a production ready component.
At least the topics below should be considered when making a production implementation:

### Consumer organisation management
The test-client uses a hard-coded list of example user-organisations with dummy passwords. In production implementation 
a proper consumer organisation database must be added. This database must map the consumer organisation's TLS 
certificate to their X-Road service-class and member-code. Also, the database must be able to store the JWT tokens 
provided by the X-Road services.

### Secrets management
The test-client uses .p12 keystores and trust-stores to store the required TLS certificates.
The store files use dummy passwords. In production implementation use an external certificate manager or at least
setup proper passwords and password management for the keystores.


## test-client unit test keystores

The keystores used by test-client unit tests are included in the git repo.
You can refer to ``test-client/src/test/resources/crete-keystores.sh`` to see how they are created.
