# Changelog

## [unreleased]

### Improvements
* JWK for /.well-known/jwks.jws configurable. Removed key defaults from main classpath,
  enforcing requirement for externally provided keys.

## [0.6.0]

### Features
* Create a job to delete the expired session nonces
* Countersigning of Mobile-ID signatures. Implements HTTP signature standard RFC9421

### Improvements
* Switched to latest Spring Boot 3 from Spring Boot 4 to resolve constant Jackson version conflicts
  between Spring Boot and SK clients (smart-id-java-client, mid-rest-java-client)
* REST endpoint input validation errors are returned as HTTP 400 Bad Request with problem details.

## [0.5.0] First public release 