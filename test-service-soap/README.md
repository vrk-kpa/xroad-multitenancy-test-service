# Multi-tenant X-Road test-service with SOAP API

## Overview
The test-service-soap is a reference implementation of an X-Road service that can handle
requests made by multiple organisations sharing a single X-Road subsystem and security server.

In a multi-tenant X-Road system the X-Road service provider is responsible for the following tasks:
 * Maintain a registry of X-Road member codes of the organisations that are allowed to use the service
 * Provide login credentials for the organisations that can be used to access the service
 * Provide a login API that can be used to exchange credentials for a JWT
 * Authorize users based on the JWT when they call the service

This service is based on the 
[X-Road Example Adapter](https://github.com/nordic-institute/xrd4j/tree/develop/example-adapter) 
implementation provided by NIIS.

## Dependencies
Other services in this repository use Java 17 and Spring Boot 3. This service is based on the NIIS
example adapter and unfortunately has different dependencies as the other services:
* Java 11
* Maven
* Docker


## What is implemented in the test-service
The test-service provides a SOAP api at the endpoint `/multitenancy-soap-test-service/Endpoint`.
The service WSDL can be found at `/multitenancy-soap-test-service/Endpoint?wsdl`.

Following SOAP services are implemented:
* `Authenticate`: Authenticate caller organisation by username and password. Returns a JWT token that can be
  used to call the `HelloService` and `GetRandom` services.
* `HelloService`: returns a greeting message with a custom name 
* `GetRandom`: returns a random number from 0 to 100

Refer to the WSDL for more technical details of the SOAP request structure.

This is a test-service that can be used to test the authentication flow. The service has no real user database. 
The `Authenticate` service generates a JWT for any username when given the mock password value `password`.


## What is not implemented

This test-service implementation is not a production ready component. 
At least the topics below should be considered when implementing a production service with multi-tenancy support:

### Consumer organisation management
As mentioned above, the test-service-soap has only a mock user management implementation, 
that authenticates any username using the mock password.

A production grade consumer organisation management implementation must implement a user management solution with 
a way to register new trusted organisations, generate credentials for them and store those credentials in a safe way in the service.

### Secrets management
The test-service uses .p12 keystores and trust-stores to store the required TLS certificates and JWT signing keys. 
The store files use dummy passwords. In production implementation use an external certificate and secrets manager should
be considered. At minimum proper passwords and password management for the keystores are a required.
