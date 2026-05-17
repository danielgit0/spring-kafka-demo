# Kafka Demo

A Spring Boot application demonstrating core Kafka patterns: producing and consuming Avro-serialized events via a Redpanda broker cluster, with a fully code-generated REST API layer and observability tooling.

## Overview

The application exposes a REST endpoint (`POST /api/v1/message/employee`) that publishes an `Employee` event to a Kafka topic. A `@KafkaListener` consumer on the same application reads the event back and logs it. The message payload is serialized using **Apache Avro** and validated against a schema registered in the **Confluent Schema Registry** embedded in Redpanda.

```
HTTP Client → REST API (OpenAPI-generated) → Kafka Producer → [employee topic] → Kafka Consumer
```

## Reference

The main Kafka sources are
* `Kafka The Definitive Guide Real-Time Data and Stream Processing at Scale Second Edition by Gwen Shapira Todd Palino Rajini Sivaram Krit Petty`.
* [Spring for Apache Kafka - 4.0.5](https://docs.spring.io/spring-kafka/reference/index.html)

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 25 (GraalVM) via SDKMAN |
| Framework | Spring Boot 4 |
| Broker | Redpanda (Kafka-compatible) |
| Serialization | Apache Avro + Confluent Schema Registry |
| REST API | Spring MVC — code generated from OpenAPI 3.1 spec |
| Event contract | AsyncAPI 3.0 spec |
| Observability | OpenTelemetry, Prometheus, Grafana, Tempo, Loki, Pyroscope |
| Build | Gradle (Kotlin DSL) |

---

## Infrastructure

The full local stack is defined in [`src/main/resources/docker/compose.yml`](src/main/resources/docker/compose.yml).

### Services

| Service | Port(s) | Description |
|---|---|---|
| `kafka-0` | `9092`, `8082`, `8083`, `9644` | Redpanda broker — bootstrap, Pandaproxy, Schema Registry, Admin |
| `kafka-1` | `9093` | Redpanda broker |
| `kafka-2` | `9094` | Redpanda broker |
| `kafka-console` | `8081` | Redpanda Console UI |
| `otel-collector` | `4317`, `4318`, `8889` | OpenTelemetry Collector |
| `tempo` | `3200` | Distributed tracing backend |
| `loki` | `3100` | Log aggregation |
| `pyroscope` | `4040` | Continuous profiling |
| `prometheus` | `9090` | Metrics scraping |
| `grafana` | `3000` | Observability dashboards |
| `schema-bootstrap` | — | One-shot container that registers Avro schemas on startup |

### Starting the stack

The project is configured for **Podman**. Start the stack from the project root:

```bash
podman compose -f src/main/resources/docker/compose.yml up -d
```

Or with Docker:

```bash
docker compose -f src/main/resources/docker/compose.yml up -d
```

> **Docker Compose auto-start (optional)**
> Spring Boot can start the compose stack automatically when the application boots.
> This is disabled by default (Podman compatibility). To enable it with Docker, set the following in `application.yml`:
> ```yaml
> spring:
>   docker:
>     compose:
>       enabled: true
> ```

### Broker addressing

Each Redpanda node exposes two listeners:

- **`PLAINTEXT`** — internal Docker network (e.g. `kafka-0:29092`) used for inter-broker communication
- **`OUTSIDE`** — host-accessible (e.g. `localhost:9092`) used by the Spring Boot app and local tools

The application is configured to bootstrap against all three nodes:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092,localhost:9093,localhost:9094
```

---

## API Specifications

### OpenAPI (REST)

The REST interface is defined in [`openapi/openapi.yml`](openapi/openapi.yml) using **OpenAPI 3.1**.

The spec is split across multiple files (requests, responses, parameters) and bundled at build time using [Redocly CLI](https://redocly.com/docs/cli/). The bundled spec is served at runtime via SpringDoc:

```
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

Gradle tasks:

```bash
./gradlew redoclyLint      # Lint the spec
./gradlew redoclyBundle    # Bundle into a single file
./gradlew openApiGenerate  # Generate Spring MVC interfaces and model classes
```

The generated code is placed under `build/generated-sources/openapi/` and compiled alongside the hand-written sources. The application implements the generated `EmployeeApi` interface directly — no manual controller boilerplate.

### AsyncAPI (Kafka events)

The Kafka contract is documented in [`asyncapi/asyncapi.yml`](asyncapi/asyncapi.yml) using **AsyncAPI 3.0**.

It describes:

- **Channel:** `employee.local.kafka_demo.employee_created.v1`
- **Operations:** `publishEmployeeCreated` (send) and `consumeEmployeeCreated` (receive)
- **Message payload:** `Employee` Avro record (`src/main/avro/Employee.avsc`)

```json
{
  "type": "record",
  "name": "Employee",
  "namespace": "com.example.generated.kafka",
  "fields": [
    { "name": "id",         "type": "string" },
    { "name": "name",       "type": "string" },
    { "name": "department", "type": "string" }
  ]
}
```

Avro Java classes are generated at build time:

```bash
./gradlew generateAvro
```

---

## Running the Application

### Prerequisites

- Java 25 (GraalVM) — use SDKMAN: `sdk env install`
- Podman or Docker with Compose support

### Steps

```bash
# 1. Start infrastructure
podman compose -f src/main/resources/docker/compose.yml up -d

# 2. Run the application
./gradlew bootRun
```

### Sending an event

```bash
curl -X POST http://localhost:8080/api/v1/message/employee \
  -H "Content-Type: application/json" \
  -d '{"id": "1", "name": "Alice", "department": "Engineering"}'
```

The application will publish the event to Kafka and the consumer will log it.

---

## Observability

| Tool | URL | Purpose |
|---|---|---|
| Grafana | http://localhost:3000 | Dashboards (metrics, traces, logs, profiles) |
| Prometheus | http://localhost:9090 | Raw metrics |
| Redpanda Console | http://localhost:8081 | Topic browser, consumer groups, schema registry |

---

## Project Structure

```
.
├── asyncapi/               # AsyncAPI 3.0 event contract
├── openapi/                # OpenAPI 3.1 REST contract (split spec)
├── src/
│   └── main/
│       ├── avro/           # Avro schema definitions (.avsc)
│       ├── java/com/example/kafka/
│       │   ├── consumer/   # @KafkaListener consumer
│       │   ├── producer/   # REST controller → Kafka producer
│       │   ├── errors/     # Global exception handling
│       │   └── common/     # Shared constants (topic names)
│       └── resources/
│           └── docker/     # Compose stack + service configs
└── build.gradle.kts
```
