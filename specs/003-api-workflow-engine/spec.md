# Feature Specification: API Workflow Engine

**Feature Branch**: `003-api-workflow-engine`

**Created**: 2026-08-07

**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Define a Multi-Step API Workflow (Priority: P1)

A tester creates a named workflow composed of an ordered sequence of API calls. Each step specifies which endpoint to call, what request body/headers to send, and — critically — which values to extract from the previous step's response and where to inject them into the current step's request. This allows full end-to-end flows such as: login → extract token → call protected endpoint → extract resource ID → update resource.

**Why this priority**: This is the foundation of the entire feature. Without the ability to define workflows with chained data, no other story is meaningful.

**Independent Test**: Create a 3-step workflow: (1) `POST /api/auth/otp/request` → extract `referenceId` from response body → (2) `POST /api/auth/otp/verify` injecting `referenceId` as a body field → (3) `POST /api/auth/oauth/token` injecting the returned `access_token` into a header. Verify the workflow definition is saved and the step dependencies are correct.

**Acceptance Scenarios**:

1. **Given** a user opens the Workflows section, **When** they click "New Workflow" and add 3 steps with names and endpoint selections, **Then** the workflow is saved with ordered steps and a unique workflow ID.
2. **Given** step 1 returns `{"data": {"token": "abc123"}}`, **When** the user configures a data extraction rule `$.data.token → variable:authToken`, **Then** that variable is available for injection in all subsequent steps.
3. **Given** a data injection rule maps `variable:authToken` to the `Authorization` header of step 2, **When** the workflow runs, **Then** step 2's actual request includes `Authorization: abc123`.
4. **Given** a step has a body field configured with a literal value and an injected variable, **When** the workflow runs, **Then** the literal value is used for the literal field and the extracted variable value is used for the injected field.

---

### User Story 2 — Run a Workflow and View Chained Results (Priority: P1)

A tester executes a saved workflow against a target base URL. Each step runs in sequence, with extracted data from the previous step injected into the current step. The results show each step's request, response, extracted values, and pass/fail status side-by-side so the tester can trace the data flow end-to-end.

**Why this priority**: A workflow is only useful if it can be executed and the data flow can be inspected.

**Independent Test**: Run the 3-step auth workflow from Story 1 against a staging environment. Verify that the run result shows all 3 steps executed, the extracted `access_token` from step 2 appears in step 3's request headers, and the final step's status is recorded.

**Acceptance Scenarios**:

1. **Given** a 3-step workflow, **When** the user clicks Run, **Then** steps execute sequentially — step 2 only starts after step 1 completes and data extraction succeeds.
2. **Given** step 1 extracts `access_token` and step 2 injects it into the `Authorization` header, **When** the run completes, **Then** the results detail view shows the resolved value of `access_token` and the actual header sent in step 2.
3. **Given** a step's request or response contains sensitive data (tokens, passwords), **When** the result is displayed, **Then** values in fields marked as "secret" are masked with `****`.
4. **Given** a workflow run completes, **When** the user views the result, **Then** the total duration, per-step duration, and overall pass/fail status are visible.

---

### User Story 3 — Add Conditions to Control Workflow Execution (Priority: P2)

A tester attaches conditions to workflow steps. A condition evaluates the previous step's response (status code, body field value, or header value) and either allows the workflow to continue, skips the current step, or halts the workflow with a failure. This enables smart flows such as: "only call the OTP verify endpoint if OTP request returned HTTP 200" or "skip the delete step if the resource does not exist (404)."

**Why this priority**: Without conditions, every workflow is a rigid linear sequence. Conditions make workflows resilient to real-world API variability and enable "happy path + conditional branch" testing.

**Independent Test**: Create a 2-step workflow where step 2 has a condition `step1.httpStatus == 200`. Run the workflow against an endpoint that returns 401. Verify step 2 is skipped and the workflow result shows "condition not met — step skipped" rather than an error.

**Acceptance Scenarios**:

1. **Given** a step has a condition `step1.httpStatus == 200`, **When** step 1 returns 401, **Then** the conditioned step is marked "skipped" and execution continues with remaining steps (or halts if configured to stop).
2. **Given** a condition references a body field `step1.body.status == "SUCCESS"`, **When** the field is absent from the response, **Then** the condition evaluates to false and the step is skipped, not errored.
3. **Given** a condition is set to "halt on failure", **When** the condition is not met, **Then** all subsequent steps are skipped and the run is marked failed.
4. **Given** a step has multiple conditions joined by AND, **When** all conditions are met, **Then** the step executes; if any condition fails, the step is skipped.

---

### User Story 4 — Update and Manage Workflows (Priority: P2)

A tester can view all saved workflows in a list, open a workflow to edit its steps, add new steps, reorder steps, update data extraction and injection rules, modify conditions, and delete workflows. Changes are saved explicitly when the user clicks Save.

**Why this priority**: Workflows are iterative artifacts — testers need to refine them as API contracts evolve.

**Independent Test**: Open an existing 3-step workflow, add a 4th step between step 2 and step 3 by dragging it into position, change step 3's condition, and save. Verify the workflow now has 4 steps in the new order with the updated condition.

**Acceptance Scenarios**:

1. **Given** a saved workflow, **When** the user opens it in edit mode, **Then** all steps, extraction rules, injection rules, and conditions are displayed and editable.
2. **Given** a workflow in edit mode, **When** the user reorders steps, **Then** data extraction variable references in later steps update to reflect the new step source.
3. **Given** a workflow in edit mode, **When** the user deletes a step that other steps reference for data extraction, **Then** dependent injection rules are highlighted as broken and the user is warned before saving.
4. **Given** the user clicks Delete Workflow, **When** they confirm the dialog, **Then** the workflow and all its run history are removed.

---

### User Story 5 — Use Workflow Variables from Global Config (Priority: P3)

A tester can define reusable named variables in the workflow settings (e.g., `baseUsername`, `testMobileNumber`) that are available as injectable values in any workflow step's request body, headers, or query params — without hard-coding them into individual steps. This separates test data from workflow structure.

**Why this priority**: Useful for parameterising workflows across environments, but the feature works without it using literal values and extracted variables.

**Independent Test**: Define a workflow variable `testMobile = +94771234567`, reference it as `{{testMobile}}` in step 1's body field `mobile_number`, run the workflow, and verify `+94771234567` appears in the outgoing request.

**Acceptance Scenarios**:

1. **Given** a workflow variable `testMobile` is defined, **When** step 1's body field uses `{{testMobile}}`, **Then** the resolved value is sent in the actual request.
2. **Given** a workflow variable is updated, **When** the workflow runs again, **Then** all steps using `{{variableName}}` pick up the new value.
3. **Given** a step body references `{{undefinedVar}}`, **When** the workflow runs, **Then** the placeholder is left unresolved, the run records a warning, and execution continues.

---

### Edge Cases

- What if an extracted JSONPath expression matches multiple values? → First match is used; the step configuration warns that the expression is ambiguous.
- What if a workflow step's endpoint no longer exists in the spec? → The step is flagged as "broken" in the workflow editor; the workflow cannot run until it is fixed.
- What if step N's extraction rule references a field that is absent from the response? → The variable is set to `null`; steps that inject `null` send an empty/omitted field and record a warning.
- What if two steps have a circular data reference? → Detected at save time; the system rejects the workflow with an error message.
- What if the workflow has 20+ steps and one step fails midway? → All remaining steps after the failure are marked skipped (or continued, depending on the step's halt-on-failure flag); the run is marked "partial".
- What if the user navigates away with unsaved workflow edits? → A confirmation dialog warns of unsaved changes.

## Requirements

### Functional Requirements

- **FR-001**: Users MUST be able to create a named workflow containing an ordered list of API steps, each mapped to an endpoint from an existing spec or defined manually.
- **FR-002**: Each workflow step MUST support configuring: HTTP method, path, request headers, request body (literal values and variable references), and query parameters.
- **FR-003**: Users MUST be able to define data extraction rules per step: a named variable, a source (response body via JSONPath, response header by name, or status code), applied to that step's response.
- **FR-004**: Users MUST be able to define data injection rules per step: a target location (request header, body field, or query parameter) mapped to an extracted variable or a workflow-level variable.
- **FR-005**: Users MUST be able to attach one or more conditions to any step, each evaluated against a prior step's response: HTTP status code comparison, body field value comparison, or header value comparison.
- **FR-006**: Conditions MUST support logical operators: equals, not equals, contains, exists, and is empty.
- **FR-007**: Steps MUST have a configurable halt-on-condition-failure flag: "continue" (default) or "halt workflow".
- **FR-008**: When a workflow runs, steps MUST execute sequentially; a step MUST NOT start until the previous step completes.
- **FR-009**: Extracted variable values from each step MUST be available for injection in all subsequent steps within the same run.
- **FR-010**: The run result MUST display per-step details: request sent (with resolved variables), response received, extracted variable values, condition evaluation results, and pass/fail/skipped status.
- **FR-011**: Users MUST be able to reorder steps in edit mode and update all dependent variable references accordingly.
- **FR-012**: Users MUST be warned when editing a step deletion would break a variable reference used by a later step.
- **FR-013**: Users MUST be able to define workflow-level variables (name → value) that are resolved as `{{variableName}}` in any step's request fields.
- **FR-014**: Users MUST be able to delete a workflow and its associated run history.
- **FR-015**: Sensitive step fields marked as "secret" MUST be masked in the UI and in run result storage.
- **FR-016**: The system MUST detect and reject circular data-reference configurations at save time.

### Key Entities

- **Workflow**: A named, ordered collection of steps. Contains workflow ID, name, description, base URL override (optional), workflow-level variables map, and an ordered list of WorkflowSteps.
- **WorkflowStep**: One API call within a workflow. Contains step ID, name, HTTP method, path, headers, body template, query params, a list of ExtractionRules, a list of InjectionRules, and a list of Conditions.
- **ExtractionRule**: Defines how to extract a value from a step's response. Contains variable name, source type (body/header/status), and a locator (JSONPath expression or header name).
- **InjectionRule**: Defines where to inject an extracted or workflow variable. Contains target type (header/body/query), target key, and variable reference.
- **Condition**: A predicate evaluated against a prior step's response. Contains source step reference, source type (status/body/header), locator, operator (eq/ne/contains/exists/empty), expected value, and action on failure (continue/halt).
- **WorkflowRun**: The result of executing a workflow. Contains run ID, workflow ID, start/end time, overall status, and a list of StepResults.
- **StepResult**: The result of one workflow step. Contains step ID, status (pass/fail/skipped/error), request sent (with resolved values), response received, extracted variable values (masked if secret), condition evaluation details, and duration.

## Success Criteria

### Measurable Outcomes

- **SC-001**: A tester can define and save a 5-step workflow with data extraction and injection rules in under 10 minutes.
- **SC-002**: A workflow run that extracts and injects data across 5 steps completes within 15 seconds on a stable network (3 seconds per step maximum).
- **SC-003**: Condition evaluation correctly skips or halts steps in 100% of test cases where the condition is not met.
- **SC-004**: Extracted variable values are correctly injected into subsequent steps in 100% of test cases where the source field is present in the response.
- **SC-005**: A tester can trace the full data flow of a completed run (what value was extracted, where it was injected) without referring to external tools.
- **SC-006**: Workflows with up to 20 steps execute without timeout or memory errors in the current Spring Boot deployment.

## Assumptions

- This feature builds on the existing API Test Agent — workflows use the same HTTP execution engine and auth/header configuration already present in the system.
- JSONPath (e.g. `$.data.token`) is the standard extraction syntax for response bodies; full JSONPath specification support is assumed via a library dependency.
- Workflow step endpoints may be defined manually (not required to exist in an ingested spec), since workflow scenarios often span multiple services.
- Real-time streaming of step results (showing each step as it completes) is desirable but not required for v1 — batch result display after completion is acceptable.
- Workflow versioning (tracking changes to a workflow over time) is out of scope for this iteration.
- Branch/parallel execution (running steps in parallel or with if/else branching) is out of scope; all workflows are linear sequences with conditional skipping only.
- Import/export of workflows as JSON files is out of scope for this iteration.
