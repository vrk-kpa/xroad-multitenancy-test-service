# Multi-tenant X-Road test-service with REST API

## Overview
The test-service is a reference implementation of an X-Road service that can handle
requests made by multiple organisations sharing a single X-Road subsystem and security server.

In a multi-tenant X-Road system the X-Road service provider is responsible for the following tasks:
 * Maintain a registry of X-Road member codes of the organisations that are allowed to use the service
 * Provide login credentials for the organisations that can be used to access the service
 * Provide a login API that can be used to exchange credentials for a JWT
 * Authorize users based on the JWT when they call the service

## What is implemented in the test-service
The test-service provides a REST api at base path `/multitenancy-rest-test-service/api` with following endpoints:

* `GET /authenticate`: Authenticate caller organisation using username and password. Returns a JWT token that can be
  used to call the `/private` endpoints.
    * required headers:
      * `X-Road-Represented-Party`: the X-Road member class and code of the calling organisation
      * `Member-Username`: the username of the calling organisation
      * `Member-Password`: the password of the calling organisation
    * return value:
      * `200` status code and `Authorization` header with the JWT token if username and password are valid.
      * `401` status code if username and password are invalid
* `GET /private/hello`: returns a random number from 0 to 100. Can only be called with a valid JWT.
  * required headers:
    * `Authorization`: the JWT token returned by `/authenticate`
    * `X-Road-Represented-Party`: the X-Road member class and code of the calling organisation
* `GET /private/random`: calls the test-service to get a greeting message:
    * required headers:
      * `Authorization`: the JWT token returned by `/authenticate`
      * `X-Road-Represented-Party`: the X-Road member class and code of the calling organisation
    * query parameters:
      * `name` the custom name to be used in the greeting

This is a test-service that can be used to test the authentication flow. The service has no real user database. 
The `/authenticate` endpoint generates a JWT to any username when given the mock password value `password`.


## What is not implemented

This test-service implementation is not a production ready component. 
At least the topics below should be considered when implementing a production service with multi-tenancy support:

### Consumer organisation management
As mentioned above, the test-service has only a mock user database, that authenticates any username using the mock password.
In a production system a database solution is needed to register the trusted organisations to the service.

### Secrets management
The test-service uses .p12 keystores and trust-stores to store the required TLS certificates and JWT signing keys. 
The store files use dummy passwords. In production implementation use an external certificate and secrets manager should
be considered. At minimum proper passwords and password management for the keystores are a required.
