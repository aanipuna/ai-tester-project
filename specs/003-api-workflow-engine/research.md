# Research: API Workflow Engine

**Branch**: `003-api-workflow-engine`
**Feature**: Multi-step API workflow with data extraction, injection, and conditions

---

## 1. JSONPath Extraction Library

**Decision**: Use `com.jayway.jsonpath:json-path` (already a transitive Spring Boot dependency via `spring-boot-starter-web`).

**Rationale**: Zero new dependencies. Supports `$.data.token`, `$[0].id`, deep scan `$..token` syntax. Available in Spring Boot classpath by default via Jackson/OGNL integration.

**Alternatives considered**: Regex extraction (fragile for structured data), manual Jackson tree traversal (verbose, not reusable).

**Usage pattern**:
```java
String value = JsonPath.read(responseBody, "$.data.token");
```
Returns `null` cleanly when path not found — safe for optional extractions.

---

## 2. Data Model Storage Strategy

**Decision**: File-based JSON storage in `data/workflows/` directory — same pattern as plans, runs, and specs (JSON files per entity, `JsonRepositorySupport` base class).

**Rationale**: Zero new infrastructure (no DB, no migration), consistent with all existing stores (`PlanJsonStore`, `RunStore`, `SpecStore`). Workflows + runs ≤500 files for typical usage — file I/O is not a bottleneck.

**New directories**:
- `data/workflows/{workflowId}.json` — workflow definitions
- `data/workflow-runs/{workflowRunId}/results.json` — run results

---

## 3. UI Pattern for Workflow Editor

**Decision**: Reuse the existing `plan-detail.html` edit-mode pattern — a table with expandable rows for step details. Step reordering via Up/Down arrow buttons (no drag-and-drop, which would require a JS library).

**Rationale**: Drag-and-drop requires an external JS library. Keyboard-accessible Up/Down buttons are simpler, work on all screen sizes, and match the project's "vanilla JS only" constraint. Consistent with existing inline-edit patterns in plan-detail.html.

---

## 4. Variable Resolution and Data Flow

**Decision**: Variables resolved at execution time using a per-run `Map<String, String> context`. Each step reads from context (injections), executes, then writes extracted values back to context. Context is reset per run.

**Pattern**:
```
context = { globalVar1: "val", globalVar2: "val" }
step1 executes → response → extract "$.data.token" → context.put("step1.authToken", "abc")
step2 reads context.get("step1.authToken") for injection → sends Authorization: Bearer abc
```

**Template syntax**: `{{variableName}}` in step body/header/path fields. Resolved before the request is built. This is identical to the AI prompt template `{{method}}` syntax already in `PromptTemplateConfig`.

---

## 5. Condition Evaluation

**Decision**: Enum-based comparator with 6 operators: `EQ, NE, CONTAINS, EXISTS, EMPTY, GT_STATUS` (HTTP status greater-than for "status < 400 means success" patterns).

**Rationale**: A switch/case evaluator is ~50 lines, needs no external expression library, is easy to test exhaustively, and covers all spec-defined operators plus the common HTTP status range check.

**Implementation**:
```java
boolean evaluate(ConditionOperator op, String actual, String expected) {
    return switch (op) {
        case EQ       -> actual != null && actual.equals(expected);
        case NE       -> !Objects.equals(actual, expected);
        case CONTAINS -> actual != null && actual.contains(expected);
        case EXISTS   -> actual != null && !actual.isBlank();
        case EMPTY    -> actual == null || actual.isBlank();
        case LT_STATUS -> Integer.parseInt(actual) < Integer.parseInt(expected);
    };
}
```

---

## 6. Existing Integration Points (No Changes Required)

| Component | Reuse |
|-----------|-------|
| `JsonRepositorySupport` | Base class for `WorkflowJsonStore` and `WorkflowRunStore` |
| `DataPathProperties` | Add `workflowsDir()` and `workflowRunsDir()` methods |
| `Ids` service | `Ids.nextWorkflowId()`, `Ids.nextWorkflowRunId()` |
| `SecretMasker` | Mask values in `StepResult` marked as `secret: true` |
| `AuthConfig` + global headers | Applied to all workflow step requests (same as plan execution) |
| `WebClient` | Same HTTP execution client as `ExecutorService` |
| `PageController` | Route `/workflows` already wired |
| Nav bar | Already added to all 6 templates on `003-api-workflow-engine` branch |

---

## 7. REST API Surface (new endpoints)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/workflows` | Create workflow |
| `GET` | `/api/workflows` | List all workflows |
| `GET` | `/api/workflows/{id}` | Get workflow |
| `PUT` | `/api/workflows/{id}` | Update workflow (full replace) |
| `DELETE` | `/api/workflows/{id}` | Delete workflow |
| `POST` | `/api/workflows/{id}/runs` | Execute workflow |
| `GET` | `/api/workflow-runs` | List all workflow runs |
| `GET` | `/api/workflow-runs/{runId}` | Get workflow run result |

---

## All NEEDS CLARIFICATION Items Resolved

All technical unknowns from the spec are resolved above. No blockers for Phase 1 design.
