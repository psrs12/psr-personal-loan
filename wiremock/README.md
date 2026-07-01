# WireMock Standalone Mock Server

WireMock provides a single stub server (port 9090) that replaces all external enterprise platform dependencies during local development and end-to-end testing.

## Services mocked

| Mapping file | Paths stubbed | Consumer |
|---|---|---|
| `bolt-tokenization.json` | `POST /tokenize` | application-management-service |
| `offer-management.json` | `GET /invitations/*/validate`, `GET /invitations/*/offer` | application-management-service |
| `customer-profile.json` | `GET /customers/*` | application-management-service |
| `ssn-verification.json` | `POST /ssn/verify` | application-management-service |
| `credit-management.json` | `POST /credit/soft-pull`, `POST /credit/hard-pull` | pricing-orchestration-service |
| `decision-platform.json` | `POST /pricing/evaluate`, `POST /decision/evaluate` | pricing-orchestration-service |

## Running WireMock standalone

### Via Docker Compose (recommended)

WireMock is included in `deploy/docker-compose.local.yml` and starts automatically with the stack:

```bash
docker compose -f deploy/docker-compose.local.yml up -d
```

WireMock admin UI is available at http://localhost:9090/__admin

### Standalone (outside Docker)

Download the WireMock standalone JAR:

```bash
curl -Lo wiremock.jar https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.9.1/wiremock-standalone-3.9.1.jar
```

Run from the repository root:

```bash
java -jar wiremock.jar --port 9090 --root-dir wiremock --verbose
```

## Toggling mock vs real integrations

Backend services use a Spring `mock` profile to point at WireMock instead of real enterprise endpoints.

**In Docker Compose** — set via `SPRING_PROFILES_ACTIVE: mock` on each service (already configured in `docker-compose.local.yml`).

**Running a service locally** — pass the profile on the command line:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mock
```

or as a JVM argument:

```bash
java -jar target/application-management-service.jar --spring.profiles.active=mock
```

Remove `mock` from `SPRING_PROFILES_ACTIVE` (or omit the profile flag) to use real integration URLs defined in `application.yml`.

## Adding new scenarios

1. Add a new mapping object to the relevant file in `wiremock/mappings/`, or create a new file for a new upstream service.
2. Use `urlPattern` with a regex for path parameters (e.g. `/invitations/.*/offer`).
3. Restart the WireMock container (or `POST /__admin/mappings/reset` to hot-reload without restart):

```bash
curl -X POST http://localhost:9090/__admin/mappings/reset
```

4. To add a stateful scenario (e.g. first call returns pending, second returns complete) use WireMock [Scenarios](https://wiremock.org/docs/stateful-behaviour/).
