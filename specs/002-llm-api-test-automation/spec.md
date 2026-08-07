# Feature Specification: LLM-Powered API Test Automation

**Feature Branch**: `002-llm-api-test-automation`

**Created**: 2026-08-07

**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Generate Meaningful Test Plans from an API Spec (Priority: P1)

A tester uploads a Postman collection or OpenAPI spec for an existing project. The system uses an LLM to analyse the endpoint definitions — including parameter names, types, and constraints — and generates a comprehensive test plan with human-readable descriptions that explain what each test case is validating and why it would pass or fail.

**Why this priority**: This is the core value proposition. Without AI-generated test cases grounded in the actual API contract, the tool is just a manual test editor.

**Independent Test**: Upload the FinTech Postman collection, generate a plan for `POST /api/auth/otp/request`, and verify the generated test cases cover positive, negative (missing `mobile_number`), boundary (empty string), and auth (no token) scenarios with descriptive labels.

**Acceptance Scenarios**:

1. **Given** a Postman collection with a POST endpoint that has 5 required body parameters, **When** the user triggers "Generate Plans", **Then** the system produces at least 6 test cases — at minimum one positive, one negative per required field, one boundary, and one auth case.
2. **Given** a generated test plan, **When** the user views it, **Then** every test case description names the parameter(s) under test and states the expected HTTP outcome in plain English.
3. **Given** the LLM is unavailable, **When** the user triggers generation, **Then** the system falls back to a deterministic rule-based plan with parameter-aware descriptions rather than showing a blank result.

---

### User Story 2 — Execute Test Plans Against a Live API and Review Results (Priority: P1)

A tester selects a generated plan and runs it against the actual API base URL. The system sends each test case's HTTP request — including body, headers, and auth — records the actual response status and body, and marks each case pass/fail based on the expected vs actual HTTP status code.

**Why this priority**: Equally critical to generation — the system has no value if it cannot execute and report results.

**Independent Test**: Select a plan for `GET /api/ezcash/profile/check`, configure a valid Bearer token in global auth settings, run the plan, and confirm that TC-004 (auth category, expected 401) passes and the others show actual status codes in the result table.

**Acceptance Scenarios**:

1. **Given** a test plan with 5 cases and a valid base URL, **When** the user clicks Run, **Then** all 5 cases execute and results (status, response body, pass/fail) are stored within 60 seconds.
2. **Given** a global Bearer token is configured in Settings, **When** the plan runs, **Then** every outgoing request includes the `Authorization: Bearer <token>` header automatically.
3. **Given** a POST endpoint with a JSON body, **When** the executor sends the request, **Then** the body is serialised and the `Content-Type: application/json` header is added only for methods that support a body (POST, PUT, PATCH).
4. **Given** a response with no body (e.g. 204 or 401 with empty body), **When** the executor receives it, **Then** the result is stored with a null response snapshot and no error is raised.

---

### User Story 3 — Edit Test Cases Before Execution (Priority: P2)

A tester can open a generated plan, adjust individual test cases (method, path, expected status, request body, and headers), add new test cases, or remove ones that are not applicable before running the plan.

**Why this priority**: AI-generated plans are a starting point. Testers must be able to correct parameter values, add environment-specific tokens, and remove irrelevant cases.

**Independent Test**: Open a plan, change TC-002's expected status from 400 to 422, add a custom `X-Tenant-ID` header, and save — then run and confirm the run report reflects the updated expectation.

**Acceptance Scenarios**:

1. **Given** an existing plan in edit mode, **When** the user changes the expected status dropdown and saves, **Then** subsequent runs evaluate against the new expected status.
2. **Given** edit mode is open, **When** the user clicks "+ Add Test Case" and fills in the fields, **Then** the new case appears in view mode and is included in the next run.
3. **Given** edit mode is open with a Headers panel expanded, **When** the user picks "Authorization: Bearer" from the standard headers dropdown and enters a value, **Then** that header is sent with the request on the next run.

---

### User Story 4 — View and Export Test Reports (Priority: P2)

After execution, a tester can view a detailed run report showing each test case's result, description, request URL, request body, response body, and failure reason. The report is also downloadable as a Markdown file and viewable as a standalone HTML page.

**Why this priority**: Results are only useful if they can be shared with a team or archived. Export formats are required for CI integration and stakeholder communication.

**Independent Test**: Run a plan, click "Build Report", open the HTML report in a browser, and confirm it contains the Description column plus response bodies for all 5 cases.

**Acceptance Scenarios**:

1. **Given** a completed run, **When** the user clicks "Build Report", **Then** the HTML and Markdown reports are generated and accessible within 5 seconds.
2. **Given** a report, **When** the user opens the HTML version, **Then** it includes columns for Case ID, Description, Category, Status, HTTP code, request URL, request body, response body, and failure reason.
3. **Given** a report, **When** the user downloads the Markdown version, **Then** it is a valid Markdown table that renders correctly in GitHub.

---

### User Story 5 — Configure Global Auth and Headers for All Requests (Priority: P3)

A tester can configure a global authentication method (Bearer token, Basic Auth, or API Key) and any number of additional headers in the Settings page. These are automatically applied to every outgoing request without requiring the tester to edit individual test cases.

**Why this priority**: Essential for testing protected APIs, but lower priority than core generation and execution flow.

**Independent Test**: Configure `Authorization: Bearer <token>` in Settings, run a plan for a protected endpoint, and verify the auth header appears in the request log.

**Acceptance Scenarios**:

1. **Given** a Bearer token is saved in global auth settings, **When** any plan runs, **Then** every request in that run includes `Authorization: Bearer <token>` regardless of individual test case configuration.
2. **Given** global headers include `X-Tenant-ID: acme`, **When** a plan runs, **Then** all requests include that header appended after auth headers.
3. **Given** auth is cleared in settings, **When** the next plan runs, **Then** no auth header is injected.

---

### Edge Cases

- What happens when the LLM returns malformed or incomplete JSON? → Fall back to rule-based plan, do not surface raw LLM output to user.
- What happens when the API base URL is unreachable during execution? → Each case records `error` status with the connection error message; other cases continue.
- What happens when a response body is binary or extremely large? → Truncate snapshot to 500 characters, mark as truncated.
- What happens when two test cases have the same ID (e.g. after merging plans)? → Last-write-wins on save; UI warns user.
- What happens when generating plans for a spec with 167 endpoints? → Generation runs sequentially with a visible elapsed timer and Cancel button; partial results are saved if cancelled.

## Requirements

### Functional Requirements

- **FR-001**: The system MUST accept Postman Collection v2.1 and OpenAPI/Swagger YAML/JSON as input sources for spec ingestion.
- **FR-002**: The system MUST use an LLM (Anthropic Claude) to generate test cases when an API key is configured, and MUST fall back to a deterministic rule-based generator otherwise.
- **FR-003**: Generated test case descriptions MUST name the parameter(s) under test and state the expected HTTP outcome in plain English.
- **FR-004**: The system MUST generate at minimum: one positive, one negative per required parameter (up to 2), one boundary, one auth (if endpoint requires auth), and one idempotency test case per endpoint.
- **FR-005**: Users MUST be able to edit test case fields (method, path, expected status, description, request body, headers) and save changes.
- **FR-006**: Users MUST be able to add unlimited custom request headers per test case and select from a standard headers dropdown.
- **FR-007**: The system MUST execute test plans by sending HTTP requests to the configured base URL and recording actual status codes and response bodies.
- **FR-008**: Request bodies MUST be sent only for POST, PUT, and PATCH methods; GET, HEAD, and DELETE MUST NOT include a body.
- **FR-009**: Global auth configuration (Bearer, Basic, API Key) MUST be applied automatically to all outgoing requests without per-test-case configuration.
- **FR-010**: Global headers configured in Settings MUST be appended to every request after auth headers.
- **FR-011**: Each test case result MUST record: case ID, description, category, status (pass/fail/error/skipped), HTTP status code, response time, request URL, request body, response snapshot, and failure reason.
- **FR-012**: Users MUST be able to build a report from a completed run, downloadable as Markdown and viewable as a standalone HTML page.
- **FR-013**: Both HTML and Markdown reports MUST include a Description column for each test case result.
- **FR-014**: Users MUST be able to delete individual plans, runs, and specs.
- **FR-015**: Users MUST be able to clear all plans at once.
- **FR-016**: The system MUST support pagination-free display of up to 200 plans in a responsive card grid without horizontal overflow.

### Key Entities

- **Spec (NormalizedSpec)**: Represents an ingested API definition. Contains spec ID, name, source type (postman/openapi), base URL, import timestamp, and a list of endpoint specs.
- **EndpointSpec**: A single API endpoint within a spec. Contains method, path, auth type, expected success status, and a list of parameter specs (name, type, location, required flag).
- **TestPlan**: A runnable test plan for one endpoint. Contains plan ID, source endpoint, base URL, status (active/archived), source spec ID, and a list of test cases.
- **TestCase**: One test case within a plan. Contains ID, category, description, expected status, enabled flag, request spec (method, path, headers, body), and timeout.
- **TestRun**: The result of executing a plan. Contains run ID, plan ID, trigger source, start/end times, a summary (total/passed/failed/errors), and a list of case results.
- **CaseResult**: The result of one test case execution. Contains case ID, description, category, status, HTTP status, response time, request URL, request body, response snapshot, and failure reason.
- **AuthConfig**: Global authentication and headers config. Contains auth type, token/credentials, and a map of global headers applied to all requests.

## Success Criteria

### Measurable Outcomes

- **SC-001**: A tester can upload a Postman collection and generate test plans for all endpoints in under 30 minutes (for a 50-endpoint collection with AI enabled).
- **SC-002**: Test plan execution completes within 5 seconds per test case on a stable network connection.
- **SC-003**: At least 80% of generated test case descriptions are meaningful without manual editing (validated by tester review).
- **SC-004**: The system produces zero "error" status results due to null pointer or serialisation bugs when executing against a live API.
- **SC-005**: HTML and Markdown reports render correctly in a browser and GitHub respectively without layout issues.
- **SC-006**: A tester can complete the full workflow (upload → generate → configure auth → run → view report) in under 10 minutes for a single endpoint.

## Assumptions

- The AI Test Agent is already built and deployed as a Spring Boot web application (this spec describes additions and quality hardening to the existing system, not a greenfield build).
- The Anthropic Claude API key is available as an environment variable; without it the system uses rule-based fallback — this is expected and acceptable.
- The primary users are software testers and developers, not end consumers; no accessibility requirements beyond basic keyboard navigability are assumed.
- Mobile device support is a nice-to-have; the primary target is desktop browsers at 1280px+ width.
- The base URL for tested APIs can vary per plan and is not a system-wide setting (only auth and headers are global).
- Parallel plan execution (running multiple plans simultaneously) is out of scope for this iteration.
- The spec does not cover CI/CD pipeline integration (webhook triggers, exit codes) — that is a future feature.
