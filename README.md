# api-test-agent

api-test-agent is a standalone Java Spring Boot application for AI-powered API testing.

It runs in both modes from a single fat JAR:
- CLI mode for scripted and automation workflows
- Local web UI mode for interactive usage

All persistence is file-based JSON under a configurable data directory. No external database is used.

## Tech Stack

- Java 17+
- Spring Boot 3.x
- Maven
- Spring Web + Thymeleaf + HTMX
- Spring WebClient for API execution
- Spring AI with Anthropic Claude for generation and narrative summary

## Prerequisites

- Java 17+
- Maven 3.9+
- Optional: ANTHROPIC_API_KEY for LLM generation/narrative

## Build

```bash
mvn clean package
```

## CLI Usage

Generate from OpenAPI:

```bash
java -jar target/api-test-agent.jar generate --spec ./samples/petstore-openapi.yaml
```

Generate from Postman collection:

```bash
java -jar target/api-test-agent.jar generate --postman ./samples/collection.json
```

Generate from manual JSON descriptor:

```bash
java -jar target/api-test-agent.jar generate --manual ./samples/manual-spec.json
```

Run a plan:

```bash
java -jar target/api-test-agent.jar run --plan plan-123
```

Generate report:

```bash
java -jar target/api-test-agent.jar report --run run-456
```

Exit behavior:
- `generate`: `0` success, `2` validation, `3` generation failure
- `run`: `0` all pass, `1` one or more fail/error, `2` validation, `4` execution/persistence
- `report`: `0` success, `2` validation, `3` report failure

## Web UI Usage

Start with no command arguments:

```bash
java -jar target/api-test-agent.jar
```

Open:
- http://localhost:8080/specs
- http://localhost:8080/plans
- http://localhost:8080/runs

## Data Layout

- `data/specs/{spec-id}.json`
- `data/plans/{plan-id}.json`
- `data/runs/{run-id}/results.json`
- `data/runs/{run-id}/report.md`
- `data/runs/{run-id}/report.html`

All persisted JSON documents include `schemaVersion`.

## Configuration

Key settings in `application.yml`:
- `api-test-agent.data-dir` default: `./data`
- `api-test-agent.storage.schema-version` default: `1.0`

## Troubleshooting

- `mvn not found`: install Maven and ensure it is on PATH.
- `Run not found`: verify `runId` exists under `data/runs` and contains `results.json`.
- `Plan not found`: verify `planId` exists under `data/plans`.
- `LLM unavailable`: generation/reporting falls back to deterministic behavior where supported.
- `Permission errors writing data`: set a writable `api-test-agent.data-dir`.
