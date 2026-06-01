# CDOC2 Relying-Party Server

## Structure

- `adapter`
    - Implementation details for data access, input and output
    - May depend on: `app`, `openapi`
- `app`
    - Business logic, completely agnostic towards data access implementation.
      Internally structured according to logical application usecases. Defines interfaces
      for any needed external data access, which are then implemented in the `adapter` module
    - May not have dependencies to other modules
- `db-changelog`
    - Liquibase changes and related helpers
    - May not have dependencies to other modules
- `openapi`
    - Openapi definition and code generation of cdoc2-auth-server REST API
    - May not have dependencies to other modules
- `webapp`
    - Spring boot application
    - May depend on `adapter`, `db-changelog`

### Running from JAR

- Create database (see README.md under /db-changelog)
- `mvn clean install`. JAR is created under /webapp/target.
- run JAR - `java -jar cdoc2-rp-server-app.jar`. Provide custom `application.properties` in same
  folder as needed

### Application properties

In configuration files, the following properties must start with the `app.` prefix:
`app.restclient.auth-server.hostUrl`

| application prop                                         | default       | description                                                                                                                                                                   |
|:---------------------------------------------------------|:--------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| restclient.auth-server.hostUrl                           |               | URL of the cdoc2-auth-server component                                                                                                                                        |
| restclient.auth-server.read-timeout                      | 5000          | read timeout for auth server requests, in millisecond                                                                                                                         |
| restclient.auth-server.hosconnection-request-timeouttUrl | 5000          | connection timeout for auth server requests, in milliseconds                                                                                                                  |
| countersign.ecPrivateKeyPem                              |               | PEM-encoded resource for the EC ES256 private key to use for MID countersignatures                                                                                            |
| well-known.publicKeys                                    |               | List of PEM-encoded resources for the public key(s) advertised by the /.well-known/jwks.jws endpoint                                                                          |
| well-known.activePublicKey                               |               | Name of the public key that corresponds to `jwt.ecPrivateKeyPem`. <br/>Must be contained in `well-known.publicKeys` and is used to derive the `kid` value for HTTP signatures |
| rp.sid.name                                              |               | Relying party name that rp-server presents to the SID services                                                                                                                |
| rp.mid.name                                              |               | Relying party name that rp-server presents to the MID services                                                                                                                |
| rp.sid.uuid                                              |               | Relying party UUID that rp-server presents to the SID services                                                                                                                |
| rp.mid.uuid                                              |               | Relying party UUID that rp-server presents to the MID services                                                                                                                |
| rp.certificate-level                                     | QUALIFIED     | The required certificate level when authenticating through SID/MID services                                                                                                   |
| rp.scheme-name                                           | smart-id-demo | Name of the SID scheme used (eg. `smart-id`)                                                                                                                                  |
| smartid.client.hostUrl                                   |               | URL of the SID RP API                                                                                                                                                         |
| session-nonce.expired.clean-up.cron                      |               | Cron expression for the session nonce clean-up job                                                                                                                            |
| session-nonce.expired.clean-up.delete-limit              | 1000          | Maximum number of expired session nonces deleted per clean-up run                                                                                                             |
| mobileid.client.hostUrl                                  |               | URL of the MID RP API.                                                                                                                                                        |
| mobileid.client.timeoutSeconds                           | 5             | timeout for MID client requests                                                                                                                                               |

### Spring properties

In configuration files, the following properties must start with the `spring.` prefix:
`spring.datasource.url`

| spring prop                  | description |
|:-----------------------------|:------------|
| datasource.url               |             | 
| datasource.username          |             | 
| datasource.password          |             | 
| datasource.driver-class-name |             | 

### SSL Bundles

Keystores and trust stores are defined with Spring SSL bundles.

Trust store example, where `somebundle` is a placeholder for an actual bundle name:

```
spring.ssl.bundle.jks.somebundle.truststore.location=truststore.jks
spring.ssl.bundle.jks.somebundle.truststore.password=changeit
spring.ssl.bundle.jks.somebundle.truststore.type=jks
```

Keystore example, where `somebundle` is a placeholder for an actual bundle name::

```
spring.ssl.bundle.jks.somebundle.keystore.location=keystore.p12
spring.ssl.bundle.jks.somebundle.keystore.password=changeit
spring.ssl.bundle.jks.somebundle.keystore.type=pkcs12
spring.ssl.bundle.jks.somebundle.key.alias=rpServerKey
```

Defined bundles:

| bundle name   | type                 | description                                                                    |
|:--------------|:---------------------|:-------------------------------------------------------------------------------|
| server-bundle | keystore, truststore | keystore and truststore (if any) to use for embedded server SSL connections    |
| sid-server    | truststore           | provides truststore for SID server connections                                 |
| mid-server    | truststore           | provides truststore for MID server connections                                 |
| trusted-infra | truststore           | provides truststore for REST clients communicating with other CDOC2 components |

### Building the docker image locally

To build Docker images:

```bash
./build-images.sh
```

To run the build container:

```bash
docker run --rm --network=host ghcr.io/open-eid/cdoc2-rp-server:0.7.0
```

### Key generation for HTTP signatures

```
openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out ec_keypair.pem \
  && openssl pkey -in ec_keypair.pem -pubout -out ec_public.pem
  
openssl ec -in ec_keypair.pem -out ec_private.pem
```