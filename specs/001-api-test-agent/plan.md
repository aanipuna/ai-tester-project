# Implementation Plan: API Test Agent

**Branch**: `001-api-test-agent` | **Date**: 2026-08-06 | **Spec**: `/specs/001-api-test-agent/spec.md`

**Input**: Feature specification from `/specs/001-api-test-agent/spec.md`

## Summary

Build a standalone Java Spring Boot application named api-test-agent that supports both CLI and local web UI from one executable fat JAR. The system ingests API definitions (OpenAPI, Postman, manual), normalizes them to a unified model, generates test scenarios through one Claude call, executes tests through WebClient, and produces deterministic plus narrative reports. Persistence is strictly local JSON files under a configurable data directory with schema versioning and atomic writes.

## Technical Context

**Language/Version**: Java 17+

**Primary Dependencies**: Spring Boot 3.x, Spring Web, Thymeleaf, HTMX (CDN), Spring AI (Anthropic Claude), Spring WebClient, Jackson

**Storage**: Local JSON files only under configurable ./data directory; no external database

**Testing**: JUnit 5, Spring Boot Test, MockWebServer or WireMock for HTTP behavior, CLI integration tests, controller integration tests

**Target Platform**: Local developer workstation (Windows/macOS/Linux) running JVM

**Project Type**: Single deployable application with dual interfaces (CLI + local web app)

**Performance Goals**:
- 95% of suites with up to 50 lightweight API tests complete in under 2 minutes
- CLI generation/run commands provide first output within 5 seconds on baseline machine
- Report generation completes within 10 seconds for runs up to 50 test cases

**Constraints**:
- One fat JAR distribution only
- No external DB or remote persistence dependencies
- Shared core logic for CLI and web, no behavior divergence
- Atomic file writes and schemaVersion in every persisted file

**Scale/Scope**:
- Single-user local operation
- Typical plan size 8 to 20 generated test cases per endpoint
- Multiple specs/plans/runs per workspace with local disk limits

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Phase 0 Gate Review

- Principle I. Code Quality Is Mandatory: PASS
  - Planned architecture isolates core business logic and uses thin adapters, minimizing duplication and complexity.
- Principle II. Testing Is a Release Gate: PASS
  - Plan includes unit, integration, contract, and CLI/web validation paths with explicit acceptance scenarios.
- Principle III. User Experience Must Be Consistent: PASS
  - Shared core services and equivalent workflows are required across CLI and web UI.
- Principle IV. Performance Budgets Are Enforceable: PASS
  - Measurable runtime targets and latency-related assertions are defined in spec and carried into plan.
- Principle V. Simplicity and Maintainability First: PASS
  - Single executable, no external database, and server-rendered UI avoid unnecessary system complexity.

### Post-Phase 1 Gate Review

- Principle I. Code Quality Is Mandatory: PASS
  - Data model and contracts define clear boundaries and validation expectations.
- Principle II. Testing Is a Release Gate: PASS
  - Quickstart defines repeatable end-to-end validation for CLI, UI, import, and recovery paths.
- Principle III. User Experience Must Be Consistent: PASS
  - REST and CLI contracts both map to the same core operations and expected outputs.
- Principle IV. Performance Budgets Are Enforceable: PASS
  - Execution and reporting flows include measurable thresholds and evidence capture.
- Principle V. Simplicity and Maintainability First: PASS
  - Design stays file-based with explicit schema evolution and avoids non-required infrastructure.

No constitution violations identified.

## Project Structure

### Documentation (this feature)

```text
specs/001-api-test-agent/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── rest-api.yaml
│   └── cli-commands.md
└── tasks.md
```

### Source Code (repository root)

```text
pom.xml
src/
├── main/
│   ├── java/com/example/apitestagent/
│   │   ├── ApiTestAgentApplication.java
│   │   ├── core/
│   │   │   ├── SpecParser.java
│   │   │   ├── ScenarioGenerator.java
│   │   │   ├── PlanStore.java
│   │   │   ├── Executor.java
│   │   │   └── ReportBuilder.java
│   │   ├── cli/
│   │   │   ├── GenerateCommand.java
│   │   │   ├── RunCommand.java
│   │   │   └── ReportCommand.java
│   │   └── web/
│   │       ├── controller/
│   │       ├── dto/
│   │       └── mapper/
│   └── resources/
│       ├── templates/
│       ├── static/
│       └── application.yml
└── test/
    ├── java/com/example/apitestagent/
    │   ├── unit/
    │   ├── integration/
    │   └── contract/
    └── resources/

samples/
├── petstore-openapi.yaml
└── sample-generated-plan.json
```

**Structure Decision**: Use a single Maven project with layered packages core, cli, and web so all business behavior is implemented once in core and consumed by both interfaces.

## Complexity Tracking

No constitution exceptions or additional complexity justifications are required at this stage.
