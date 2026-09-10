## Running locally (localhost)

This file describes how to run `cdoc2-rp-server` in your local development machine, without
external infrastructure.

### Installing and creating PostgreSQL DB in Docker

#### Install PostgreSQL in Docker
(Docker must be installed)

From `db-changelog` directory run:
```bash
cd db-changelog
docker compose up -d
```
This starts a Postgres container (`db-rp`, see `db-changelog/docker-compose.yml`) listening on
`localhost:7433`.

#### Create DB
From `db-changelog` directory run:
```bash
mvn clean compile liquibase:update
```

### Compiling the servers
From `cdoc2-rp-server` directory run:
```bash
mvn clean install
```

### Running
(psql in docker must be running)

From `webapp` directory run:
```bash
java -jar target/cdoc2-rp-server-webapp-VER.jar
```
where VER is the version of the package built by `mvn install` previously (e.g.
`target/cdoc2-rp-server-webapp-0.8.0.jar`).

The bundled `webapp/src/main/resources/application.properties` already ships a localhost dev
configuration (self-signed keystore/truststores, connection to the dockerized Postgres started
above, demo SID/MID service URLs), so the server can be started as-is without providing
`-Dspring.config.location`. Provide a custom `application.properties` in the same folder as the
jar if you need to override any property (see README.md for the full list of `app.*` /
`spring.*` properties).

Or run the server with `-Dlogging.config=target/test-classes/logback.xml` if you need to see logs.

Note: to enable TLS handshake debugging, add `-Djavax.net.debug=ssl:handshake` option.

The logging format can be changed by providing logback configuration.
An example OpenTelemetry-compatible Logback configuration is included in `otel-logback.xml`.
To include the logback configuration, use the `-Dlogging.config` JVM option.

Example of running the server with `otel-logback.xml`:
```
java -Dlogging.config=webapp/src/main/resources/otel-logback.xml -jar target/cdoc2-rp-server-webapp-VER.jar
```

By default the server listens on `https://localhost:7600` and its actuator (management) endpoints
on `https://localhost:17600`.

`cdoc2-rp-server` calls out to `cdoc2-auth-server` for parts of the MID/SID flow
(`app.restclient.auth-server.hostUrl`, defaults to `https://localhost:7500`). See
`cdoc2-auth-server/getting-started.md` if you want both servers running locally to exercise the
full flow.

#Testing
### Check that the server is up
```bash
curl -k https://localhost:17600/actuator/health
```
Response:
```json
{"status":"UP","components":{"db":{"status":"UP","details":{"database":"PostgreSQL","validationQuery":"isValid()"}},"livenessState":{"status":"UP"},"readinessState":{"status":"UP"}}}
```

### Fetch signing keys
```bash
curl -k https://localhost:7600/.well-known/jwks.jws
```
Returns the public key(s) (`app.well-known.publicKeys`) that `cdoc2-rp-server` uses to sign MID/SID
countersignatures.

### Create a session nonce
```bash
curl -i -k -X POST https://localhost:7600/session_nonce
```
Response:
```
HTTP/1.1 200
{"nonce":"..."}
```
