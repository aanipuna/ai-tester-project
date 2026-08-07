# Tasks: AI-Driven API Workflow Chaining

**Branch**: `004-ai-workflow-chaining`
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md) | **Model**: [data-model.md](data-model.md) | **Contracts**: [contracts/rest-api.md](contracts/rest-api.md)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Extend existing infrastructure with AI workflow directories and ID generators.

- [ ] T001 Add `aiWorkflowsDir()` and `aiWorkflowRunsDir()` to `src/main/java/com/dialog/dtg/core/config/DataPathProperties.java`
- [ ] T002 [P] Add `nextAiWorkflowId()`, `nextAiWorkflowRunId()`, and `nextAiStepId()` to `src/main/java/com/dialog/dtg/core/service/Ids.java`

**Checkpoint**: DataPathProperties + Ids ready — model and store implementation can begin.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain models and stores all user stories depend on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T003 Create `AiWorkflow.java` with all fields in `src/main/java/com/dialog/dtg/core/model/AiWorkflow.java`
- [ ] T004 [P] Create `AiWorkflowStep.java` in `src/main/java/com/dialog/dtg/core/model/AiWorkflowStep.java`
- [ ] T005 [P] Create `AiWorkflowRun.java` in `src/main/java/com/dialog/dtg/core/model/AiWorkflowRun.java`
- [ ] T006 [P] Create `AiStepResult.java` in `src/main/java/com/dialog/dtg/core/model/AiStepResult.java`
- [ ] T007 [P] Create `ResolvedField.java` in `src/main/java/com/dialog/dtg/core/model/ResolvedField.java`
- [ ] T008 Create `AiWorkflowStore.java` extending `JsonRepositorySupport` with save/get/list/delete in `src/main/java/com/dialog/dtg/core/store/AiWorkflowStore.java`
- [ ] T009 Create `AiWorkflowRunStore.java` extending `JsonRepositorySupport` with save/get/list/delete in `src/main/java/com/dialog/dtg/core/store/AiWorkflowRunStore.java`
- [ ] T010 [P] Create `AiWorkflowListItem.java` DTO (slim list response) in `src/main/java/com/dialog/dtg/web/dto/AiWorkflowListItem.java`

**Checkpoint**: All models and stores ready — user story implementation can begin in parallel.

---

## Phase 3: User Story 1 — Build an AI Workflow from Existing Spec Endpoints (Priority: P1) 🎯 MVP

**Goal**: Users can create an AI workflow by selecting a spec and picking ordered endpoints. No manual rules needed.

**Independent Test**: POST to `/api/ai-workflows` with specId and 3 endpointIds from that spec; GET it back; verify steps are persisted with endpoint references. Navigate to `/workflows` and confirm the AI Workflows section shows the new workflow.

- [ ] T011 [US1] Implement `AiWorkflowController.java` with: `POST /api/ai-workflows`, `GET /api/ai-workflows`, `GET /api/ai-workflows/{id}`, `PUT /api/ai-workflows/{id}`, `DELETE /api/ai-workflows/{id}` in `src/main/java/com/dialog/dtg/web/controller/AiWorkflowController.java`
- [ ] T012 [US1] Add validation in `AiWorkflowController` — verify `specId` exists in SpecStore and each `endpointId` exists in that spec's endpoint list
- [ ] T013 [US1] Add `GET /ai-workflows/new`, `GET /ai-workflows/{id}`, `GET /ai-workflow-runs/{runId}` routes and `POST /ui/ai-workflows/{id}/delete` to `PageController.java` in `src/main/java/com/dialog/dtg/web/controller/PageController.java`
- [ ] T014 [US1] Create `ai-workflow-edit.html` — spec dropdown, endpoint multi-step picker (shows endpoint list from selected spec as selectable items in order), name input, Save button in `src/main/resources/templates/ai-workflow-edit.html`
- [ ] T015 [US1] Add JavaScript to `ai-workflow-edit.html` — on spec selection, fetch `/api/specs/{specId}` to load endpoint list; allow user to add endpoints in order with ↑↓ reorder and ✕ remove; submit saves via `POST /api/ai-workflows`
- [ ] T016 [US1] Create `ai-workflow-detail.html` — workflow metadata, ordered step list showing endpoint method+path from spec, Run/Edit/Delete buttons, and recent runs section in `src/main/resources/templates/ai-workflow-detail.html`
- [ ] T017 [US1] Add **AI Workflows** section to `workflows.html` — grid of AI workflow cards (same `.wf-card` style as manual workflows) with name, spec badge, step count, Run/Details/Delete buttons in `src/main/resources/templates/workflows.html`
- [ ] T018 [US1] Add `POST /ui/ai-workflows/{id}/run` form route to `PageController.java` — redirect to run result after execution

**Checkpoint**: User Story 1 complete — AI workflows can be created, viewed, and deleted. No execution yet.

---

## Phase 4: User Story 2 — AI Automatically Chains API Calls (Priority: P1)

**Goal**: LLM receives prior response + next endpoint spec definition and returns resolved request values.

**Independent Test**: Run a 2-step AI workflow (OTP request → OTP verify) via `POST /api/ai-workflows/{id}/runs`. Verify step 2's `resolvedRequest.body` contains a `referenceId` value extracted from step 1's response by the AI.

- [ ] T019 [US2] Create `AiWorkflowExecutorService.java` — sequential step execution, builds LLM prompt per step, parses response, constructs resolved request, sends HTTP request via WebClient, stores `AiStepResult` in `src/main/java/com/dialog/dtg/core/service/AiWorkflowExecutorService.java`
- [ ] T020 [US2] Implement `buildResolutionPrompt()` in `AiWorkflowExecutorService` — constructs prompt with: prior step response JSON, next step's `EndpointSpec` parameters (name, type, location, required), and accumulated run context (all prior step responses)
- [ ] T021 [US2] Implement `parseAiResolution()` in `AiWorkflowExecutorService` — parses LLM JSON response into `resolvedFields`, `requestBody`, `headerOverrides`, `queryParamOverrides`; handles malformed JSON with fallback to empty + warning
- [ ] T022 [US2] Implement `executeStep()` in `AiWorkflowExecutorService` — applies AI-resolved values and global auth/headers, sends HTTP request via existing `WebClient` pattern, records `AiStepResult` with `RequestSnapshot`, response body, and resolved fields
- [ ] T023 [US2] Handle LLM unavailability gracefully in `AiWorkflowExecutorService` — if `ChatClient` is null, set run status to `llm_unavailable`, skip all steps, return structured error run result
- [ ] T024 [US2] Add `POST /api/ai-workflows/{workflowId}/runs`, `GET /api/ai-workflow-runs`, `GET /api/ai-workflow-runs/{runId}` to `AiWorkflowController.java` in `src/main/java/com/dialog/dtg/web/controller/AiWorkflowController.java`

**Checkpoint**: User Story 2 complete — AI executes multi-step API chains with automatic field resolution.

---

## Phase 5: User Story 3 — View AI-Resolved Values and Reasoning in Run Result (Priority: P1)

**Goal**: Run result shows per-step resolved request, response, AI reasoning notes per field, and pass/fail.

**Independent Test**: After running a 2-step workflow, open `/ai-workflow-runs/{runId}`. Verify step 2 shows `resolvedFields` with `reasoningNote` entries, the actual resolved request body, and the HTTP status badge.

- [ ] T025 [US3] Create `ai-workflow-run-detail.html` — per-step result cards: resolved request (method + URL + headers + body), HTTP status badge, response body snippet, per-field `resolvedFields` table (fieldName, value, source, locator, reasoningNote), failure reason in `src/main/resources/templates/ai-workflow-run-detail.html`
- [ ] T026 [US3] Add dark mode support to `ai-workflow-run-detail.html` — step result cards, resolved fields table, response body snippet using existing dark-mode CSS patterns
- [ ] T027 [US3] Add run history section to `ai-workflow-detail.html` — table of recent runs (runId, status badge, startedAt, step count, link to detail) in `src/main/resources/templates/ai-workflow-detail.html`

**Checkpoint**: User Story 3 complete — full observability of AI decisions per run.

---

## Phase 6: User Story 4 — Manage AI Workflows (Priority: P2)

**Goal**: Edit step order, add/remove steps, delete workflows.

**Independent Test**: Open an existing AI workflow, move step 2 to position 1, add a new endpoint as step 3, save. Run the workflow and verify the new step order is respected.

- [ ] T028 [US4] Add edit mode to `ai-workflow-detail.html` or link to `ai-workflow-edit.html` — reorder steps with ↑↓, add/remove steps from spec endpoint list, update name, Save changes via `PUT /api/ai-workflows/{id}`
- [ ] T029 [US4] Add unsaved-change guard to `ai-workflow-edit.html` — `beforeunload` warning if edits pending in `src/main/resources/templates/ai-workflow-edit.html`

**Checkpoint**: User Story 4 complete — full lifecycle management for AI workflows.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T030 [P] Add dark mode CSS to `ai-workflow-edit.html` — spec dropdown, endpoint list, step cards in `src/main/resources/templates/ai-workflow-edit.html`
- [ ] T031 [P] Add dark mode CSS to `ai-workflow-detail.html` — step list cards, run history table in `src/main/resources/templates/ai-workflow-detail.html`
- [ ] T032 Rebuild: `mvn package -DskipTests` — verify BUILD SUCCESS and app starts correctly
- [ ] T033 Commit all `004-ai-workflow-chaining` branch changes and push to origin

---

## Dependencies

```
Phase 1 (T001-T002)
  └─> Phase 2 (T003-T010)
        ├─> Phase 3 - US1 (T011-T018)   ←─ CRUD + UI
        ├─> Phase 4 - US2 (T019-T024)   ←─ depends on T011 (AiWorkflowController) + T003-T007 (models)
        ├─> Phase 5 - US3 (T025-T027)   ←─ depends on T019-T024 (executor)
        └─> Phase 6 - US4 (T028-T029)   ←─ depends on T014-T016 (edit + detail UI)
                          └─> Phase 7 Polish (T030-T033)
```

## Parallel Execution Opportunities

- **Phase 2**: T004–T007 (all model classes) and T010 (DTO) can be created simultaneously.
- **Phase 3**: T014–T016 (templates) and T011 (controller) can be developed in parallel after T008-T009 (stores) are ready.
- **Phase 7**: T030 and T031 are fully independent polish tasks.

---

## Implementation Strategy

**MVP = Phases 1 + 2 + 3 + 4 (T001–T024)**

Phase 3 (US1) first — validates data model, stores, REST API, and UI scaffolding before the complex AI executor. Then Phase 4 (US2) adds the LLM intelligence. Phase 5 (US3) adds run observability. Phase 6 (US4) adds edit management.

---

## Format Validation

All tasks follow: `- [ ] [TaskID] [P?] [Story?] Description with file path`

- T001–T002: Setup (no story label)
- T003–T010: Foundational (no story label)
- T011–T018: [US1] label
- T019–T024: [US2] label
- T025–T027: [US3] label
- T028–T029: [US4] label
- T030–T033: Polish (no story label)

**Total tasks**: 33
**Story breakdown**: US1=8, US2=6, US3=3, US4=2, Setup+Foundation+Polish=14
**Parallel opportunities**: T002, T004–T007, T010, T014–T016, T030–T031
**MVP scope**: T001–T024 (Phases 1–4, complete AI execution pipeline)
