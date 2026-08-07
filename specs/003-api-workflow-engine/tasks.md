# Tasks: API Workflow Engine

**Branch**: `003-api-workflow-engine`
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md) | **Model**: [data-model.md](data-model.md)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: New storage directories and DataPathProperties wiring — no user story can begin without this.

- [X] T001 Add `workflowsDir()` and `workflowRunsDir()` methods to `src/main/java/com/dialog/dtg/core/config/DataPathProperties.java`
- [X] T002 [P] Add `workflowId` and `workflowRunId` generators to `src/main/java/com/dialog/dtg/core/service/Ids.java`
- [X] T003 [P] Add `json-path` version property to `pom.xml` (verify `com.jayway.jsonpath:json-path` is available as transitive dep; declare explicit if not)

**Checkpoint**: DataPathProperties + Ids ready — model and store work can begin in parallel.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain models, stores, and base execution infrastructure all user stories depend on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T004 Create `Workflow.java` with all fields in `src/main/java/com/dialog/dtg/core/model/Workflow.java`
- [X] T005 [P] Create `WorkflowStep.java` in `src/main/java/com/dialog/dtg/core/model/WorkflowStep.java`
- [X] T006 [P] Create `ExtractionRule.java` in `src/main/java/com/dialog/dtg/core/model/ExtractionRule.java`
- [X] T007 [P] Create `InjectionRule.java` in `src/main/java/com/dialog/dtg/core/model/InjectionRule.java`
- [X] T008 [P] Create `Condition.java` with `ConditionOperator` and `ConditionSource` enums in `src/main/java/com/dialog/dtg/core/model/Condition.java`
- [X] T009 [P] Create `WorkflowRun.java` in `src/main/java/com/dialog/dtg/core/model/WorkflowRun.java`
- [X] T010 [P] Create `StepResult.java`, `RequestSnapshot.java`, and `ConditionResult.java` in `src/main/java/com/dialog/dtg/core/model/`
- [X] T011 Create `WorkflowJsonStore.java` extending `JsonRepositorySupport` with save/get/list/delete in `src/main/java/com/dialog/dtg/core/store/WorkflowJsonStore.java`
- [X] T012 Create `WorkflowRunStore.java` extending `JsonRepositorySupport` with save/get/list in `src/main/java/com/dialog/dtg/core/store/WorkflowRunStore.java`
- [X] T013 Create `ConditionEvaluator.java` with enum-switch evaluate(operator, actual, expected) → boolean in `src/main/java/com/dialog/dtg/core/service/ConditionEvaluator.java`

**Checkpoint**: All models and stores ready — user story implementation can begin.

---

## Phase 3: User Story 1 — Define a Multi-Step API Workflow (Priority: P1) 🎯 MVP

**Goal**: Users can create, save, retrieve, update, and delete workflows with steps, extraction rules, and injection rules via UI and REST API.

**Independent Test**: Create a 3-step workflow via `POST /api/workflows`, GET it back, verify step order and extraction/injection rules are preserved. Navigate to `/workflows`, confirm it appears; open workflow detail, confirm steps display correctly.

- [X] T014 [US1] Implement `WorkflowController.java` REST endpoints: `POST /api/workflows`, `GET /api/workflows`, `GET /api/workflows/{id}`, `PUT /api/workflows/{id}`, `DELETE /api/workflows/{id}` in `src/main/java/com/dialog/dtg/web/controller/WorkflowController.java`
- [X] T015 [US1] Add `GET /workflows`, `GET /workflows/{workflowId}` routes to `PageController.java` in `src/main/java/com/dialog/dtg/web/controller/PageController.java`
- [X] T016 [US1] Implement `workflows.html` — full list page: workflow cards (name, step count, status badge, Run/Edit/Delete buttons) replacing placeholder in `src/main/resources/templates/workflows.html`
- [X] T017 [US1] Create `workflow-detail.html` — view mode: per-step accordion showing method, path, extraction rules, injection rules; Edit and Run buttons in `src/main/resources/templates/workflow-detail.html`
- [X] T018 [US1] Add edit mode to `workflow-detail.html` — inline form for step fields (name, method, path, body, headers), extraction rule editor (variable name, source, locator, secret toggle), injection rule editor (target, key, variable ref) in `src/main/resources/templates/workflow-detail.html`
- [X] T019 [US1] Add workflow-level variables editor to `workflow-detail.html` edit mode — key/value pair list with Add/Remove using same pattern as global headers editor in settings in `src/main/resources/templates/workflow-detail.html`
- [X] T020 [US1] Add step reorder (Up/Down arrow buttons) to `workflow-detail.html` edit mode — JavaScript updates step order in DOM; save serialises final order in `src/main/resources/templates/workflow-detail.html`
- [X] T021 [US1] Add broken-reference warning in `workflow-detail.html` edit mode — detect injection rules referencing variables from steps that no longer precede the current step; highlight with warning badge in `src/main/resources/templates/workflow-detail.html`
- [X] T022 [US1] Add `POST /ui/workflows/{workflowId}/delete` form route to `PageController.java` for delete button form submit in `src/main/java/com/dialog/dtg/web/controller/PageController.java`
- [X] T023 [P] [US1] Create `WorkflowListItem.java` DTO (slim list response) in `src/main/java/com/dialog/dtg/web/dto/WorkflowListItem.java`

**Checkpoint**: User Story 1 fully functional — workflows can be created, edited, reordered, and deleted independently of execution.

---

## Phase 4: User Story 2 — Run a Workflow and View Chained Results (Priority: P1)

**Goal**: Users can execute a workflow, see extracted variable values flowing between steps, and inspect each step's resolved request and actual response.

**Independent Test**: Run the 3-step auth workflow created in US1 via `POST /api/workflows/{id}/runs`. Verify the `WorkflowRun` result has all 3 StepResults, `step1.referenceId` extracted value is non-null, and step 2's `requestSent.body` contains the injected value.

- [X] T024 [US2] Create `WorkflowExecutorService.java` — sequential step execution, variable context map, extraction via JSONPath (body/header/status), injection into request fields, secret masking in `src/main/java/com/dialog/dtg/core/service/WorkflowExecutorService.java`
- [X] T025 [US2] Implement variable template resolution in `WorkflowExecutorService` — replace `{{varName}}` and `{{stepN.varName}}` in method, path, headers, body, queryParams before each step executes
- [X] T026 [US2] Implement global auth + global headers injection in `WorkflowExecutorService` — apply `AuthConfigStore.load()` auth and global headers to every step request (same as existing `ExecutorService.applyAuth()`)
- [X] T027 [US2] Add `POST /api/workflows/{workflowId}/runs` endpoint to `WorkflowController.java`; add `GET /api/workflow-runs`, `GET /api/workflow-runs/{runId}` in `src/main/java/com/dialog/dtg/web/controller/WorkflowController.java`
- [X] T028 [US2] Add `POST /ui/workflows/{workflowId}/run` form route to `PageController.java` with redirect to run result in `src/main/java/com/dialog/dtg/web/controller/PageController.java`
- [X] T029 [US2] Add `GET /workflow-runs/{runId}` route to `PageController.java` in `src/main/java/com/dialog/dtg/web/controller/PageController.java`
- [X] T030 [US2] Create `workflow-run-detail.html` — per-step result cards showing: step name, status badge, resolved request (method + URL + headers + body), HTTP status, response body snippet, extracted variables (masked if secret), response time in `src/main/resources/templates/workflow-run-detail.html`
- [X] T031 [US2] Add run history section to `workflow-detail.html` view mode — table of past runs (run ID, date, status, duration, step summary) with link to run detail in `src/main/resources/templates/workflow-detail.html`
- [X] T032 [P] [US2] Add `GET /workflow-runs` (all runs) listing to `workflows.html` or as a separate section — recent workflow runs across all workflows in `src/main/resources/templates/workflows.html`

**Checkpoint**: User Story 2 fully functional — can execute a workflow and trace data flow end-to-end without US3 conditions.

---

## Phase 5: User Story 3 — Conditions to Control Workflow Execution (Priority: P2)

**Goal**: Steps can be conditionally skipped or halt the workflow based on prior step responses.

**Independent Test**: Create a 2-step workflow where step 2 has `EQ` condition on step 1 status = 200. Run against an endpoint returning 401. Verify step 2 status = `SKIPPED`, run status = `partial`, and `conditionResults` in the StepResult shows the failing condition.

- [X] T033 [US3] Integrate `ConditionEvaluator` into `WorkflowExecutorService` — evaluate all step conditions before executing each step; record `ConditionResult` list per step; apply halt-on-failure logic in `src/main/java/com/dialog/dtg/core/service/WorkflowExecutorService.java`
- [X] T034 [US3] Handle null/missing locator response gracefully in `ConditionEvaluator` — absent body field evaluates EXISTS → false, EMPTY → true, others → false (not an error)
- [X] T035 [US3] Add conditions editor to `workflow-detail.html` edit mode — per-step collapsible conditions panel: source step selector, source type (STATUS/BODY/HEADER), locator field (shown for BODY/HEADER), operator dropdown, expected value, halt-on-failure toggle in `src/main/resources/templates/workflow-detail.html`
- [X] T036 [US3] Show condition evaluation results in `workflow-run-detail.html` step result cards — per-condition row: operator, actual value, expected value, passed/failed icon in `src/main/resources/templates/workflow-run-detail.html`
- [X] T037 [US3] Add AND-joining of multiple conditions on same step — all conditions must pass for step to execute; show combined result with individual condition breakdown in run detail in `src/main/java/com/dialog/dtg/core/service/WorkflowExecutorService.java`

**Checkpoint**: User Story 3 fully functional — conditions evaluated, steps skip/halt correctly, results traceable in UI.

---

## Phase 6: User Story 4 — Update and Manage Workflows (Priority: P2)

**Goal**: Full CRUD with reorder, broken-reference detection, and unsaved-change protection.

**Independent Test**: Open a 3-step workflow, move step 3 to position 2 (click ↑), change step 2's condition, save. Reload the page and confirm step order and condition are persisted correctly.

- [X] T038 [US4] Wire Up/Down arrow click handlers in `workflow-detail.html` edit mode JavaScript — swap adjacent step rows in DOM table; update `data-step-index` attributes after each swap in `src/main/resources/templates/workflow-detail.html`
- [X] T039 [US4] Add unsaved-change guard to `workflow-detail.html` — `beforeunload` event warns if edits are in progress and user navigates away without saving in `src/main/resources/templates/workflow-detail.html`
- [X] T040 [US4] Add circular reference detection in `WorkflowController` save path — reject workflow where any injection's `variableRef` resolves to a variable extracted by a later step (validate in `POST` and `PUT`) in `src/main/java/com/dialog/dtg/web/controller/WorkflowController.java`

**Checkpoint**: User Story 4 complete — workflows are safely editable with all guard rails in place.

---

## Phase 7: User Story 5 — Workflow-Level Variables (Priority: P3)

**Goal**: Users define reusable named variables at the workflow level, resolved as `{{varName}}` in any step field.

**Independent Test**: Define workflow variable `testMobile = +94771234567`, reference it in step 1 body as `{{testMobile}}`, run, verify the actual request body in run result contains `+94771234567`.

- [X] T041 [US5] Implement `{{varName}}` resolution in `WorkflowExecutorService.resolveTemplate()` — workflow-level variables are loaded into the context map first (lowest priority); step extraction variables override them in `src/main/java/com/dialog/dtg/core/service/WorkflowExecutorService.java`
- [X] T042 [US5] Add unresolved-variable warning in `WorkflowExecutorService` — if a `{{varName}}` placeholder remains after resolution (variable not in context), leave the placeholder as-is, add a warning to `StepResult.failureReason` and continue execution in `src/main/java/com/dialog/dtg/core/service/WorkflowExecutorService.java`

**Checkpoint**: User Story 5 complete — workflow variables fully parameterise all step fields.

---

## Phase 8: Polish & Cross-Cutting Concerns

- [X] T043 [P] Add dark mode CSS support to `workflow-detail.html` — verify `.plan-card`-equivalent dark mode overrides apply correctly to workflow step cards in `src/main/resources/templates/workflow-detail.html`
- [X] T044 [P] Add dark mode CSS support to `workflow-run-detail.html` — step result cards, condition result rows in `src/main/resources/templates/workflow-run-detail.html`
- [X] T045 [P] Update `start.bat` comment to note `data/workflows/` and `data/workflow-runs/` are created automatically at first use in `start.bat`
- [X] T046 Rebuild and verify full application: `mvn package -DskipTests` succeeds; navigate to `/workflows` in browser; existing Specs/Plans/Runs/Settings pages unaffected
- [X] T047 Commit all changes on `003-api-workflow-engine` branch and push

---

## Dependencies

```
Phase 1 (T001-T003)
  └─> Phase 2 (T004-T013)
        ├─> Phase 3 - US1 (T014-T023)   ←─ can start US2 in parallel after T011+T012 done
        ├─> Phase 4 - US2 (T024-T032)   ←─ depends on T014 (WorkflowController) for run endpoint
        ├─> Phase 5 - US3 (T033-T037)   ←─ depends on T024 (WorkflowExecutorService)
        ├─> Phase 6 - US4 (T038-T040)   ←─ depends on T014-T021 (full edit UI)
        └─> Phase 7 - US5 (T041-T042)   ←─ depends on T024 (resolver in executor)
                          └─> Phase 8 Polish (T043-T047)
```

## Parallel Execution Opportunities

**Within Phase 2**: T005-T010 (all model classes) can be created in parallel.

**US1 + US2 foundation**: After T011 (WorkflowJsonStore) and T012 (WorkflowRunStore) are done, T014 (WorkflowController CRUD) and T024 (WorkflowExecutorService setup) can proceed in parallel.

**Polish tasks**: T043, T044, T045 are fully independent — can run any time after their respective pages exist.

---

## Implementation Strategy

**MVP = Phase 1 + Phase 2 + Phase 3 (US1)**

Deliver US1 first: creates, saves, displays, and manages workflows. No execution needed — this validates the entire data model, storage, REST API, and UI scaffolding.

Then US2 adds execution — the most complex service layer work.

US3 (conditions) and US4/US5 layer on top of the working execution path.

---

## Format Validation

All tasks follow: `- [ ] [TaskID] [P?] [Story?] Description with file path`

- T001–T003: Setup (no story label)
- T004–T013: Foundational (no story label)
- T014–T023: [US1] label
- T024–T032: [US2] label
- T033–T037: [US3] label
- T038–T040: [US4] label
- T041–T042: [US5] label
- T043–T047: Polish (no story label)

**Total tasks**: 47
**Story breakdown**: US1=10, US2=9, US3=5, US4=3, US5=2, Setup+Foundation+Polish=18
**Parallel opportunities**: T002, T003 (setup); T005–T010 (models); T023 (DTO); T032 (run listing); T043–T045 (polish)
**MVP scope**: T001–T023 (Phases 1–3, US1 only — workflow CRUD fully functional)

