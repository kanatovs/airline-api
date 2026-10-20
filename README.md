# Airline API

A Java and Spring Boot REST API for storing and managing flight records in
PostgreSQL. This is a learning and portfolio project; the current build version
is `0.0.1-SNAPSHOT`.

## Technology stack

The [Gradle build](build.gradle) and wrapper configure:

- Java 17 toolchain.
- Spring Boot 4.0.2 and Spring MVC.
- Spring Data JPA with Hibernate and the PostgreSQL JDBC driver.
- Gradle 9.3.0 wrapper and Spring dependency management plugin 1.1.7.
- JUnit Jupiter through Spring Boot test starters.
- A bundled HTML, CSS and JavaScript demo under `src/main/resources/static`.

There is no configured CI workflow, Docker environment or Flyway dependency.

## Implemented features

- List all flights and retrieve a flight by ID.
- Create, update and delete flight records.
- Persist flight data through Spring Data JPA in PostgreSQL.
- Serve the bundled static demo at `/`. Its JavaScript uses a different data
  model from the API, so the page is not a verified working CRUD client.

There is no authentication or authorization. The backend has no search,
pagination, booking, passenger or payment endpoints.

## Architecture

```text
HTTP -> FlightController -> FlightServiceImpl -> FlightRepository -> PostgreSQL
```

The [controller](src/main/java/com/example/airlineapi/flight/FlightController.java)
maps HTTP requests to the
[service](src/main/java/com/example/airlineapi/flight/service/FlightServiceImpl.java).
The service delegates persistence to a
[Spring Data JPA repository](src/main/java/com/example/airlineapi/flight/repository/FlightRepository.java).
The [Flight entity](src/main/java/com/example/airlineapi/flight/domain/Flight.java)
maps to the `flights` table. Constructor injection connects the layers.

`FlightFactory`, `Printable` and the generic in-memory `DataPool` are also present;
they do not participate in the HTTP persistence flow.

## API

Default base URL: `http://localhost:8080`. JSON responses contain the entity or
array directly, without a custom response envelope.

| Method | Path | Implemented behavior | Success status |
| --- | --- | --- | --- |
| GET | `/flights` | Return all stored flights | 200 |
| GET | `/flights/{id}` | Return one existing flight | 200 |
| POST | `/flights` | Save a flight and return it with its generated ID | 201 |
| PUT | `/flights/{id}` | Replace the four mutable fields of an existing flight | 200 |
| DELETE | `/flights/{id}` | Delete by ID, with no response body | 204 |

Flight fields:

| Field | Type | Storage / meaning |
| --- | --- | --- |
| `id` | Integer | Database-generated identity; omit when creating |
| `flightNumber` | String | Non-null, up to 10 characters |
| `fromAirport` | String | Non-null, up to 3 characters |
| `toAirport` | String | Non-null, up to 3 characters |
| `departureTime` | String | Non-null ISO local date-time, e.g. `2026-11-01T10:00:00`; no timezone offset |

These are persistence constraints, not request-validation rules. Missing IDs in
reads/updates currently throw an unhandled exception; a consistent 404 response
is not implemented. There is no PATCH endpoint or custom error contract.

## Database and configuration

[application.properties](src/main/resources/application.properties) contains no
database URL, username or password. Spring Boot reads datasource settings from
the process environment. The application still enables
`spring.jpa.hibernate.ddl-auto=update`: Hibernate creates or updates mapped
tables when the application context starts. The database itself must already
exist, and the configured role needs schema creation/update permissions.

There are no versioned SQL migrations or automatic seed records. Hibernate schema
updates are a development convenience, not a migration history.

Set the datasource through process environment variables:

| Environment variable | Recommended local value / behavior |
| --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/airline` |
| `SPRING_DATASOURCE_USERNAME` | `airline_app` |
| `SPRING_DATASOURCE_PASSWORD` | Set interactively; never commit it |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Existing default: `update` |
| `SPRING_JPA_SHOW_SQL` | Existing default: `true` |
| `SERVER_PORT` | Spring Boot default: `8080` |
| `JAVA_HOME` | Installation directory of a JDK compatible with the Java 17 toolchain |

[.env.example](.env.example) documents the local settings and leaves the password
blank. It is a reference file: Spring Boot and the Gradle wrapper do not load
`.env` automatically. Use the PowerShell commands below to set the environment;
copying the example to `.env` alone does not configure the application. Local
`.env` files are ignored by Git; never put real credentials into the example.

A database password was previously committed. It has been removed from the
current configuration, but remains in published Git history. If it was used for
a real database, its owner must rotate it. Removing the source value does not
revoke that credential or erase repository history.

## Local setup: Windows PowerShell

Requirements: Git, JDK 17, and a running local PostgreSQL server. Ensure `java`
and `psql` are available in your terminal. Gradle is provided by the wrapper;
the first build needs network access to download dependencies.

Run the following from the repository root.

### 1. Create development databases

Connect with a local PostgreSQL administrator:

```powershell
psql -U postgres -d postgres
```

In the `psql` prompt, create a dedicated role and two empty databases once.
`\password` asks for a password without putting it into this README or a SQL
command in shell history:

```sql
CREATE ROLE airline_app LOGIN;
\password airline_app
CREATE DATABASE airline OWNER airline_app;
CREATE DATABASE airline_test OWNER airline_app;
\q
```

Use the development database for manual requests and the separate test database
for the context-load test. Do not point either workflow at a database containing
data you need to preserve.

### 2. Set process configuration

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/airline'
$env:SPRING_DATASOURCE_USERNAME = 'airline_app'
$env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Database password' -AsSecureString)).Password
java -version
.\gradlew.bat --version
```

Enter the password assigned with `\password airline_app`. The prompt hides it
and keeps the plaintext value out of the command itself. These variables apply
to this PowerShell session and its child processes. Set them again in any
terminal used to build or run the application.

### 3. Build and test

The `DataSourceConfigurationTests` verify environment-based datasource binding
and guard against checked-in connection settings. Run them without PostgreSQL:

```powershell
.\gradlew.bat test --tests '*DataSourceConfigurationTests'
```

The [context-load test](src/test/java/com/example/airlineapi/AirlineApi2ApplicationTests.java)
uses `@SpringBootTest` and requires PostgreSQL. Hibernate can create/update tables
during this test. It does not verify HTTP endpoints or service behavior. Run the
full suite against the separate test database:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/airline_test'
.\gradlew.bat test
.\gradlew.bat build
```

Gradle writes the test report to `build/reports/tests/test/index.html` and build
artifacts to `build/libs`. `build` includes the test task; it is not a
database-independent compilation check. To compile and package separately:

```powershell
.\gradlew.bat bootJar
```

### 4. Run

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/airline'
.\gradlew.bat bootRun
```

Stop with Ctrl+C. The API uses port 8080 unless `SERVER_PORT` is set.

## Example requests

With the application running, use a second PowerShell terminal. These requests
create, update and delete one development record:

```powershell
$base = 'http://localhost:8080'
Invoke-RestMethod -Uri "$base/flights"

$body = @{
    flightNumber = 'KZ100'
    fromAirport = 'ALA'
    toAirport = 'NQZ'
    departureTime = '2026-11-01T10:00:00'
} | ConvertTo-Json
$flight = Invoke-RestMethod -Method Post -Uri "$base/flights" -ContentType 'application/json' -Body $body
$flightUri = "$base/flights/$($flight.id)"
Invoke-RestMethod -Uri $flightUri

$replacement = @{
    flightNumber = 'KZ101'
    fromAirport = 'NQZ'
    toAirport = 'ALA'
    departureTime = '2026-11-02T11:30:00'
} | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri $flightUri -ContentType 'application/json' -Body $replacement
Invoke-WebRequest -Method Delete -Uri $flightUri
```

## Status and limitations

- The backend implements basic flight CRUD for a learning project. No production
  deployment or usage is claimed.
- Request DTO validation, consistent error mapping, access control, pagination
  and business rules such as airport-code validation are not implemented.
- The controller allows all CORS origins. Review configuration and credentials
  before exposing the application beyond local development.
- The static demo expects `flightId`, `origin`, `destination` and `capacity`;
  the backend exposes `id`, `fromAirport`, `toAirport` and no capacity field.
  Rendering and mutations need alignment before that demo is a usable client.
- Datasource configuration and context loading have tests; there are no dedicated
  controller, service or repository tests. No CI pipeline or automated migration
  system is present.
- A previously committed database password remains in published Git history;
  rotate it if it was used for a real database.
