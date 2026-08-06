# Tasks: API Test Agent

**Input**: Design documents from `/specs/001-api-test-agent/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Automated tests are included because the project constitution requires testing as a release gate.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize Maven Spring Boot project skeleton, configuration, and baseline assets.

- [x] T001 Initialize Maven project and parent Spring Boot setup in pom.xml
- [x] T002 Add required dependencies and plugins in pom.xml
- [x] T003 Create package skeleton for core, cli, and web layers in src/main/java/com/example/apitestagent/
- [x] T004 [P] Add baseline application configuration properties in src/main/resources/application.yml
- [x] T005 [P] Create root README with project overview and prerequisites in README.md
- [x] T006 [P] Add sample OpenAPI and sample generated plan placeholders in samples/petstore-openapi.yaml and samples/sample-generated-plan.json

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build shared domain models, file storage, mode switching, and cross-cutting infrastructure required by all stories.

**⚠️ CRITICAL**: No user story implementation starts before this phase is complete.

- [x] T007 Implement application entrypoint with CLI/web mode detection in src/main/java/com/example/apitestagent/ApiTestAgentApplication.java
- [x] T008 [P] Implement data directory configuration and path resolver in src/main/java/com/example/apitestagent/core/config/DataPathProperties.java
- [x] T009 [P] Create shared domain models from data model spec in src/main/java/com/example/apitestagent/core/model/
- [x] T010 Implement JSON mapper configuration with schemaVersion handling in src/main/java/com/example/apitestagent/core/config/JacksonConfig.java
- [x] T011 Implement atomic file write utility (temp then rename) in src/main/java/com/example/apitestagent/core/store/AtomicFileWriter.java
- [x] T012 Implement file lock manager for bounded concurrent access in src/main/java/com/example/apitestagent/core/store/FileLockManager.java
- [x] T013 Implement base JSON repository helpers for specs/plans/runs in src/main/java/com/example/apitestagent/core/store/JsonRepositorySupport.java
- [x] T014 [P] Implement shared exception hierarchy and error codes in src/main/java/com/example/apitestagent/core/error/
- [x] T015 [P] Implement secret masking and safe logging helpers in src/main/java/com/example/apitestagent/core/security/SecretMasker.java
- [x] T016 [P] Add baseline test configuration and fixtures for unit/integration tests in src/test/resources/application-test.yml

**Checkpoint**: Foundation ready; user story work can proceed.

---

## Phase 3: User Story 1 - Run Automated API Tests from CLI (Priority: P1) 🎯 MVP

**Goal**: Enable complete CLI flow for spec ingestion, scenario generation, test execution, and report generation.

**Independent Test**: Run `generate`, `run`, and `report` CLI commands end-to-end and verify persisted artifacts and exit codes without starting web server.

### Tests for User Story 1

- [x] T017 [P] [US1] Add CLI integration test for generate command success/error cases in src/test/java/com/example/apitestagent/integration/cli/GenerateCommandIT.java
- [x] T018 [P] [US1] Add CLI integration test for run command pass/fail exit codes in src/test/java/com/example/apitestagent/integration/cli/RunCommandIT.java
- [x] T019 [P] [US1] Add CLI integration test for report command outputs in src/test/java/com/example/apitestagent/integration/cli/ReportCommandIT.java
- [x] T020 [P] [US1] Add unit tests for assertion evaluation and result aggregation in src/test/java/com/example/apitestagent/unit/core/ExecutorTest.java

### Implementation for User Story 1

- [x] T021 [US1] Implement spec normalization service for OpenAPI, Postman, and manual inputs in src/main/java/com/example/apitestagent/core/SpecParser.java
- [x] T022 [US1] Implement spec persistence repository in src/main/java/com/example/apitestagent/core/store/SpecStore.java
- [x] T023 [US1] Implement scenario generation service using exact prompt template and Spring AI call in src/main/java/com/example/apitestagent/core/ScenarioGenerator.java
- [x] T024 [US1] Implement plan CRUD and import logic service in src/main/java/com/example/apitestagent/core/PlanStore.java
- [x] T025 [US1] Implement HTTP execution engine with WebClient and evidence capture in src/main/java/com/example/apitestagent/core/Executor.java
- [x] T026 [US1] Implement deterministic metrics plus LLM narrative report builder in src/main/java/com/example/apitestagent/core/ReportBuilder.java
- [x] T027 [US1] Implement CLI generate command adapter in src/main/java/com/example/apitestagent/cli/GenerateCommand.java
- [x] T028 [US1] Implement CLI run command adapter with non-zero fail exit handling in src/main/java/com/example/apitestagent/cli/RunCommand.java
- [x] T029 [US1] Implement CLI report command adapter and artifact path output in src/main/java/com/example/apitestagent/cli/ReportCommand.java
- [x] T030 [US1] Wire CLI command dispatch and mode-specific startup behavior in src/main/java/com/example/apitestagent/cli/CliRunner.java

**Checkpoint**: User Story 1 is fully functional as MVP via CLI only.

---

## Phase 4: User Story 2 - Create and Run Tests in Local Web UI (Priority: P2)

**Goal**: Provide local server-rendered web UI for ingesting specs, generating/editing plans, running tests, and viewing reports.

**Independent Test**: Start app with no args, perform full workflow in browser, and verify outcomes and artifacts without using CLI commands.

### Tests for User Story 2

- [x] T031 [P] [US2] Add REST contract tests for plan and run endpoints in src/test/java/com/example/apitestagent/contract/web/PlanRunApiContractTest.java
- [x] T032 [P] [US2] Add web integration test for spec upload and plan generation flow in src/test/java/com/example/apitestagent/integration/web/SpecToPlanFlowIT.java
- [x] T033 [P] [US2] Add web integration test for run execution and report access flow in src/test/java/com/example/apitestagent/integration/web/RunAndReportFlowIT.java

### Implementation for User Story 2

- [x] T034 [US2] Implement REST controller for spec ingestion and retrieval in src/main/java/com/example/apitestagent/web/controller/SpecController.java
- [x] T035 [US2] Implement REST controller for plan CRUD and import operations in src/main/java/com/example/apitestagent/web/controller/PlanController.java
- [x] T036 [US2] Implement REST controller for run execution and report endpoints in src/main/java/com/example/apitestagent/web/controller/RunController.java
- [x] T037 [US2] Implement web DTOs and request/response mappers in src/main/java/com/example/apitestagent/web/dto/ and src/main/java/com/example/apitestagent/web/mapper/
- [x] T038 [US2] Implement Thymeleaf page template for spec upload and normalization status in src/main/resources/templates/specs.html
- [x] T039 [US2] Implement Thymeleaf page template for plan editor with case enable/disable actions in src/main/resources/templates/plans.html
- [x] T040 [US2] Implement Thymeleaf page template for run results and report access in src/main/resources/templates/runs.html
- [x] T041 [US2] Implement HTMX fragments for incremental run progress and result refresh in src/main/resources/templates/fragments/run-status.html
- [x] T042 [US2] Add static styles and minimal UX consistency rules in src/main/resources/static/styles.css

**Checkpoint**: User Stories 1 and 2 are independently testable; web UI uses same core services as CLI.

---

## Phase 5: User Story 3 - Persist and Reuse Test Assets Locally (Priority: P3)

**Goal**: Ensure durable local file persistence, reload across restarts, and safe recovery from data issues.

**Independent Test**: Save assets, restart app, confirm reload; simulate malformed persistence files and validate safe recovery guidance.

### Tests for User Story 3

- [x] T043 [P] [US3] Add persistence integration test for restart reload of specs/plans/runs in src/test/java/com/example/apitestagent/integration/persistence/RestartReloadIT.java
- [x] T044 [P] [US3] Add persistence integration test for atomic write and lock contention handling in src/test/java/com/example/apitestagent/integration/persistence/AtomicWriteLockingIT.java
- [x] T045 [P] [US3] Add persistence integration test for corrupted file recovery behavior in src/test/java/com/example/apitestagent/integration/persistence/CorruptionRecoveryIT.java

### Implementation for User Story 3

- [x] T046 [US3] Implement run results repository and directory lifecycle manager in src/main/java/com/example/apitestagent/core/store/RunStore.java
- [x] T047 [US3] Implement report artifact persistence service for markdown and html files in src/main/java/com/example/apitestagent/core/store/ReportStore.java
- [x] T048 [US3] Implement schema migration/recovery coordinator for persisted JSON documents in src/main/java/com/example/apitestagent/core/store/SchemaMigrationService.java
- [x] T049 [US3] Implement corrupted file backup and restoration policy in src/main/java/com/example/apitestagent/core/store/RecoveryService.java
- [x] T050 [US3] Add startup data validation and recovery guidance messaging in src/main/java/com/example/apitestagent/core/StartupValidationService.java
- [x] T051 [US3] Add web and CLI user-facing recovery/error guidance output in src/main/java/com/example/apitestagent/web/controller/ErrorAdvice.java and src/main/java/com/example/apitestagent/cli/CliErrorFormatter.java

**Checkpoint**: All stories functional with durable local persistence and recovery safeguards.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final hardening across stories, documentation, and performance verification.

- [x] T052 [P] Add unit tests for prompt builder and JSON response validation in src/test/java/com/example/apitestagent/unit/core/ScenarioGeneratorTest.java
- [x] T053 [P] Add performance-focused integration test for 50-case suite timing budget in src/test/java/com/example/apitestagent/integration/performance/SuitePerformanceIT.java
- [x] T054 Improve logging structure and observability fields across core services in src/main/java/com/example/apitestagent/core/
- [x] T055 Harden sensitive output masking for CLI, web, and persisted artifacts in src/main/java/com/example/apitestagent/core/security/SecretMasker.java
- [x] T056 Update README with complete CLI/web usage, packaging, and troubleshooting in README.md
- [x] T057 Validate and refresh sample artifacts for manual testing in samples/petstore-openapi.yaml and samples/sample-generated-plan.json
- [x] T058 Run full quickstart verification and record final notes in specs/001-api-test-agent/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup): no dependencies.
- Phase 2 (Foundational): depends on Phase 1 and blocks all user stories.
- Phase 3 (US1): depends on Phase 2.
- Phase 4 (US2): depends on Phase 2; may run in parallel with US1 after shared core interfaces stabilize.
- Phase 5 (US3): depends on Phase 2; can begin after core stores from US1 exist.
- Phase 6 (Polish): depends on completion of all targeted user stories.

### User Story Dependencies

- US1 (P1): no dependency on other user stories.
- US2 (P2): functionally independent but reuses core services built in US1.
- US3 (P3): functionally independent but extends shared persistence behavior used by US1/US2.

### Suggested Completion Order

1. Setup
2. Foundational
3. US1 (MVP)
4. US2
5. US3
6. Polish

---

## Parallel Opportunities

- Phase 1: T004, T005, T006 can run in parallel after T001 and T002.
- Phase 2: T008, T009, T014, T015, T016 can run in parallel after T007 starts baseline bootstrapping.
- US1: T017, T018, T019, T020 can run in parallel; T027, T028, T029 can run in parallel after core services are ready.
- US2: T031, T032, T033 can run in parallel; T034, T035, T036 can run in parallel before UI templates.
- US3: T043, T044, T045 can run in parallel; T046 and T047 can run in parallel before T048-T051.
- Polish: T052 and T053 can run in parallel; T056 and T057 can run in parallel.

---

## Parallel Example: User Story 1

```bash
# Run US1 test tasks in parallel:
T017  T018  T019  T020

# Run CLI adapter tasks in parallel after core services:
T027  T028  T029
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Deliver Phase 3 (US1) end-to-end.
3. Validate CLI generate/run/report workflows against quickstart.
4. Demo or release MVP as CLI-first artifact.

### Incremental Delivery

1. Add US2 for local web UX while preserving CLI parity through shared core services.
2. Add US3 for persistence durability and recovery behavior.
3. Complete Phase 6 hardening and docs before broader usage.

### Parallel Team Strategy

1. Team completes Setup and Foundational together.
2. Then split by story track:
   - Engineer A: US1 CLI and generation/execution/report core integration.
   - Engineer B: US2 web controllers/templates/HTMX.
   - Engineer C: US3 persistence durability and recovery.
