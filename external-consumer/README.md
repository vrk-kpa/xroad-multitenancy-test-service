# External Consumer Scripts

## Overview

This directory contains scripts used to make test calls to the multi-tenancy test-client.
The scripts make curl requests to test-client using certificates of three mock organisations, `org1`, `org2` and `org3`.
`org1` and `org2` are consumers that are registered to the test-client (hard-coded). `org3` can be used to
test that calls from a non-registered organisation are rejected.

## Usage

Follow the steps in the main [readme](../README.md) to set up the multi-tenancy services in docker compose with 
all required certificates. Then, you can make calls to the test-client using the test-request.sh script.
E.g. to request a greeting form test-service-soap as org1, run:

```bash
./test-request.sh org1 "hello?name=mikko&prorocol=soap"
```
