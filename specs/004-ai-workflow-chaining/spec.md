# Feature Specification: AI-Driven API Workflow Chaining

**Feature Branch**: `004-ai-workflow-chaining`

**Created**: 2026-08-07

**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Build a Chained API Workflow by Selecting Endpoints from Existing Specs (Priority: P1)

A tester opens the AI Workflow Chaining page, selects a spec (e.g. FinTech_HUB - STG), and picks an ordered list of endpoints from that spec to form a workflow chain. They give the workflow a name and click "Create AI Workflow". No extraction rules, injection rules, or variable mapping is required — the AI does that automatically at runtime.

**Why this priority**: This is the core user-facing entry point. Without endpoint selection, no AI chaining can happen.

**Independent Test**: Select spec FinTech_HUB - STG, pick three endpoints: `POST /api/auth/otp/request`, `POST /api/auth/otp/verify`, `POST /api/auth/oauth/token`. Click Create. Verify the AI workflow is saved with those three steps in order, and that no manual rules are required.

**Acceptance Scenarios**:

1. **Given** a spec with 10+ endpoints is selected, **When** the user picks 3 endpoints and clicks Create, **Then** the workflow is saved with those 3 steps referencing the spec's endpoint definitions.
2. **Given** an endpoint has documented parameters (body fields, query params) from the spec, **When** the workflow is saved, **Then** those parameter definitions are embedded in each step for the AI to use during execution.
3. **Given** two workflows reference the same spec, **When** the spec is updated (re-ingested), **Then** the workflow steps retain their endpoint references but new runs use the latest spec parameter definitions.

---

### User Story 2 — AI Automatically Extracts Response Values and Injects Them into the Next API Call (Priority: P1)

When the workflow runs, the system sends step 1's request, then passes the response JSON to the LLM with step 2's endpoint definition. The AI identifies which fields in the response map to which fields in step 2's request, constructs the request body/headers, sends step 2, and repeats for each subsequent step.

**Why this priority**: This is the key differentiating intelligence — without AI-driven injection, this is just a dumb sequential runner.

**Independent Test**: Create a 2-step workflow: step 1 = `POST /api/auth/otp/request`, step 2 = `POST /api/auth/otp/verify`. Run it. Without any manual rule, step 2's request should include a `referenceId` or `otpToken` value extracted from step 1's response, determined by the AI.

**Acceptance Scenarios**:

1. **Given** step 1 returns `{"DATA": {"referenceId": "abc-123"}}` and step 2's spec shows it requires a `referenceId` field, **When** the AI processes this, **Then** step 2's outgoing request body contains `{"referenceId": "abc-123"}`.
2. **Given** step 1 returns a Bearer token in the response body, **When** step 3 is a protected endpoint, **Then** the AI injects the token as `Authorization: Bearer <token>` in step 3's headers.
3. **Given** the LLM cannot determine a suitable mapping (the response has no recognizable values for step 2's parameters), **When** the AI processes this, **Then** the step is executed with best-effort values and a warning is recorded in the result explaining what could not be mapped.
4. **Given** a run is executing a 5-step chain, **When** the AI has resolved all fields for each step, **Then** every step's actual request is stored in the run result so the tester can inspect exactly what was sent.

---

### User Story 3 — Review AI-Resolved Values in the Run Result (Priority: P1)

After a run completes, the tester can see each step's resolved request (the actual values the AI chose), the response received, the AI's reasoning for each field mapping (a brief explanation), and the pass/fail status based on expected vs actual HTTP status codes.

**Why this priority**: Without observability into what the AI decided, the tester cannot trust or debug the chain. This is essential for any production use.

**Independent Test**: Run a 3-step workflow. Open the run result. For each step, verify: the resolved request body shows actual values (not `{{placeholders}}`), and there is an AI reasoning note like "Extracted referenceId from step 1 response.DATA.referenceId".

**Acceptance Scenarios**:

1. **Given** a completed run, **When** the user opens the run result, **Then** each step shows the actual outgoing request (method, URL, headers, resolved body) alongside the AI's field-mapping explanation.
2. **Given** the AI mapped a value, **When** the tester inspects the run result, **Then** the AI reasoning is shown as a human-readable note per mapped field (e.g. "Mapped `referenceId` from step 1 response body field `DATA.referenceId`").
3. **Given** a step failed (wrong status code), **When** viewing the run result, **Then** the failure reason includes both the expected and actual status code, and the AI's field choices so the tester can assess whether the mapping was correct.

---

### User Story 4 — Manage AI Workflows (Priority: P2)

A tester can view all AI workflows in a list, edit the endpoint order (add/remove steps, reorder), update the workflow name, and delete AI workflows. Editing does not require re-specifying extraction rules since the AI handles that at runtime.

**Why this priority**: Workflows need lifecycle management. Once created, testers will need to iterate on them.

**Independent Test**: Create a 3-step workflow, then edit it to add a 4th step and move step 2 to position 3. Save. Verify the run uses the new step order.

**Acceptance Scenarios**:

1. **Given** a saved AI workflow, **When** the user edits the step order and saves, **Then** the next run uses the new order.
2. **Given** a tester wants to remove a step, **When** they delete a step and save, **Then** subsequent runs skip that step.
3. **Given** the user deletes a workflow, **When** they confirm, **Then** the workflow and all associated run history are removed.

---

### Edge Cases

- What if step N's response is empty or not valid JSON? → AI records "could not parse response" and attempts step N+1 with empty context from step N.
- What if the LLM API is unavailable during a run? → The run fails with status `llm_unavailable`; no partial injection is attempted; the raw response from each completed step is stored.
- What if the spec has no parameter definitions for an endpoint? → AI uses only the response context from prior steps without schema guidance; this is a best-effort extraction.
- What if a response contains sensitive fields (passwords, tokens)? → Values matching secret patterns (tokens, passwords, keys) are masked in the stored run result but still injected into subsequent requests.

## Requirements

### Functional Requirements

- **FR-001**: Users MUST be able to create an AI workflow by selecting an existing ingested spec and picking an ordered list of its endpoint definitions.
- **FR-002**: An AI workflow step MUST reference the endpoint's full definition from the spec (method, path, parameters, expected status) so the LLM has context.
- **FR-003**: When a workflow runs, the system MUST send each step's request using the global auth and header settings, then pass the response JSON + the next step's endpoint definition to the LLM.
- **FR-004**: The LLM MUST return a structured JSON payload specifying the resolved field values for the next step's request (body fields, header overrides, query params) along with a reasoning note per field.
- **FR-005**: The system MUST construct and send the next step's request using the LLM's resolved values.
- **FR-006**: If the LLM cannot resolve a required field, the system MUST record a warning and use a null/empty value rather than failing the entire run.
- **FR-007**: The run result MUST store per step: the actual outgoing request (with resolved values), the response received, the LLM's reasoning notes, and the pass/fail status.
- **FR-008**: Sensitive values (tokens, secrets) in run results MUST be masked in stored and displayed output while still being used for injection.
- **FR-009**: Users MUST be able to add, remove, and reorder steps in an AI workflow.
- **FR-010**: Deleting an AI workflow MUST also delete all its run history.
- **FR-011**: The LLM prompt for field resolution MUST include: the prior step's full response body, the next step's endpoint definition (path, method, parameters from spec), and any values already extracted in earlier steps in the current run.
- **FR-012**: The system MUST fall back gracefully if the LLM API is unavailable, recording the failure without crashing.

### Key Entities

- **AiWorkflow**: A named ordered sequence of AiWorkflowSteps. Contains workflowId, name, description, specId (source spec), steps list, createdAt, updatedAt, status.
- **AiWorkflowStep**: One step in an AI workflow. Contains stepId, name, endpointId (reference to NormalizedSpec endpoint), expectedStatus (from spec or overridden).
- **AiWorkflowRun**: The result of executing an AI workflow. Contains runId, workflowId, workflowName, startedAt, finishedAt, overall status, and a list of AiStepResults.
- **AiStepResult**: The result of one step. Contains stepId, stepName, resolvedRequest (actual values sent), httpStatus, responseBody, aiReasoningNotes (list of field→source mappings), status, failureReason.
- **ResolvedField**: A single field resolved by the AI. Contains fieldName, resolvedValue, source (which prior step response it came from), locator (JSON path), and reasoningNote.

## Success Criteria

### Measurable Outcomes

- **SC-001**: A tester can create a 3-step AI workflow from an existing spec in under 3 minutes with no manual rule configuration.
- **SC-002**: In at least 70% of test runs against real APIs, the AI correctly identifies and injects the primary chaining value (e.g. a reference ID or token) from one response into the next request.
- **SC-003**: Every run result shows the actual request values chosen by the AI and the reasoning, so no "black box" behavior.
- **SC-004**: AI workflow execution completes within 10 seconds per step (including LLM call latency).
- **SC-005**: The system never crashes or returns a 500 error when the LLM API is unavailable — always returns a structured error run result.

## Assumptions

- The existing spec ingestion infrastructure (NormalizedSpec, EndpointSpec, ParameterSpec) is already in place and will be reused to provide parameter context to the LLM.
- The Anthropic Claude API key is configured via the existing `AuthConfig`/application.yml mechanism.
- The existing `ChatClient` (Spring AI) used for plan generation will be reused for field resolution prompts.
- The AI workflow engine is a separate feature from the manual workflow engine (003) — they are independent; manual workflows remain unchanged.
- The accuracy of AI field mapping is best-effort and improves with richer spec parameter definitions; no machine learning or fine-tuning is in scope.
- The feature operates on the existing `003-api-workflow-engine` branch's new `/workflows` nav item and page infrastructure.
