# Research: API Test Agent

## Decision 1: Single Fat JAR with Runtime Mode Switch (CLI vs Web)
- Decision: Use one Spring Boot application entry point and choose runtime mode by arguments. CLI invocations set spring.main.web-application-type=none so embedded servlet container does not start; no-arg runs default web mode.
- Rationale: Preserves one distribution artifact, reduces CLI startup overhead, and avoids duplicated bootstrapping logic.
- Alternatives considered: Separate CLI and web binaries (higher operational complexity), profile-only switching (less explicit UX), always-on server with CLI endpoints (wasted resources and slower CLI feedback).

## Decision 2: Shared Core Service Layer for Both Interfaces
- Decision: Put all business logic in core services (spec normalization, scenario generation, plan CRUD, execution, report building). Keep cli and web layers as thin adapters.
- Rationale: Enforces behavior parity, minimizes drift, and directly satisfies the no-duplication architectural requirement.
- Alternatives considered: Duplicated handlers per interface (high defect risk), remote call from CLI to web API (adds network coupling to local use case).

## Decision 3: JSON File Persistence with Atomic Writes and Schema Versioning
- Decision: Store all persisted artifacts as JSON files under configurable data directory with schemaVersion in every file. Use write-to-temp-then-atomic-rename for writes and backup/recovery on parse failures.
- Rationale: Meets no-database constraint while improving durability and forward migration capability.
- Alternatives considered: Direct overwrite writes (corruption risk), embedded database (violates constraints), schema-less persistence (breaks upgrade safety).

## Decision 4: Concurrency Control for Local File Access
- Decision: Use per-resource locking with bounded wait and fail-fast error reporting for conflicting writes between CLI and web operations.
- Rationale: Prevents race conditions and partial writes when both interfaces access same artifacts.
- Alternatives considered: In-process synchronized blocks only (insufficient cross-process safety), no locking (high corruption risk), external lock service (unnecessary complexity).

## Decision 5: Spring AI + Claude in Single-Call Structured JSON Mode
- Decision: Use one Spring AI call to Anthropic Claude for scenario generation with strict output schema and response validation before persist.
- Rationale: Aligns with required one-shot comprehensive prompt while improving deterministic machine-readable output.
- Alternatives considered: Multi-call generation pipeline (slower and costlier), prompt-only JSON without schema guardrails (higher invalid output risk), custom parser loops (unnecessary complexity).

## Decision 6: Execution Engine with WebClient and Assertion Evidence Capture
- Decision: Execute plan test cases through Spring WebClient and capture status, latency, selected headers/body evidence, and assertion outcomes into raw run artifacts.
- Rationale: Supports asynchronous/non-blocking request handling and rich diagnostics needed for reproducible failures.
- Alternatives considered: RestTemplate (legacy and less flexible), shelling out to curl (platform variability, poor integration).

## Decision 7: Report Generation Split into Deterministic Metrics + LLM Narrative
- Decision: Compute counts, category breakdowns, and slow/flaky flags via deterministic logic, then generate short narrative summary with one LLM call.
- Rationale: Keeps critical metrics trustworthy while adding concise human-readable interpretation.
- Alternatives considered: Full LLM-generated reports (lower determinism), deterministic text only (lower readability).

## Decision 8: Web UI Stack Without Frontend Build Pipeline
- Decision: Use Thymeleaf server-rendered pages and HTMX from CDN for incremental interactivity.
- Rationale: Meets no npm/webpack constraint and keeps distribution simple for local standalone use.
- Alternatives considered: SPA framework with bundler (violates constraints), fully static pages with full reloads (poorer UX for run monitoring).

## Resolved Clarifications
- No unresolved NEEDS CLARIFICATION items remain after technical decisions.
- Target runtime baseline: Java 17+ and Spring Boot 3.x with Maven.
- External database remains explicitly out of scope; all persistence remains file-based.
