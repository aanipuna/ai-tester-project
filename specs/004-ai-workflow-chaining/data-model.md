# Data Model: AI-Driven API Workflow Chaining

**Branch**: `004-ai-workflow-chaining`

---

## Entities

### AiWorkflow

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `workflowId` | String | Yes | Format: `aiwf-{8hex}` |
| `name` | String | Yes | User-supplied name |
| `description` | String | No | |
| `specId` | String | Yes | FK to `NormalizedSpec` — the spec from which endpoints are drawn |
| `steps` | `List<AiWorkflowStep>` | Yes | Ordered; minimum 1 |
| `createdAt` | Instant | Yes | |
| `updatedAt` | Instant | Yes | |
| `status` | String | Yes | `active` \| `archived` |

---

### AiWorkflowStep

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `stepId` | String | Yes | Format: `aistep-{8hex}` |
| `name` | String | Yes | User-supplied or defaulted from endpoint |
| `endpointId` | String | Yes | FK to `EndpointSpec.endpointId` within the spec |
| `expectedStatus` | Integer | No | Overrides spec default; null = use spec's `expectedSuccessStatus` |

**Runtime resolution**: At execution time, the full `EndpointSpec` (method, path, parameters) is loaded from `SpecStore` using `specId` + `endpointId`.

---

### AiWorkflowRun

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `runId` | String | Yes | Format: `aiwfrun-{8hex}` |
| `workflowId` | String | Yes | |
| `workflowName` | String | Yes | Snapshot at run time |
| `startedAt` | Instant | Yes | |
| `finishedAt` | Instant | Yes | |
| `status` | String | Yes | `passed` \| `failed` \| `partial` \| `llm_unavailable` \| `error` |
| `stepResults` | `List<AiStepResult>` | Yes | |

---

### AiStepResult

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `stepId` | String | Yes | |
| `stepName` | String | Yes | |
| `endpointRef` | String | Yes | `{method} {path}` snapshot |
| `resolvedRequest` | `RequestSnapshot` | No | Actual request sent (reuses existing model) |
| `httpStatus` | Integer | No | Actual response code |
| `responseBody` | String | No | Truncated to 2000 chars |
| `resolvedFields` | `List<ResolvedField>` | No | AI's per-field mapping decisions |
| `status` | String | Yes | `pass` \| `fail` \| `skipped` \| `error` |
| `failureReason` | String | No | |
| `responseTimeMs` | Long | No | |

---

### ResolvedField

Represents one field value decision made by the AI.

| Field | Type | Notes |
|-------|------|-------|
| `fieldName` | String | The parameter/header/query name |
| `value` | String | The resolved value (masked if sensitive) |
| `source` | String | `previous_response` \| `extracted` \| `default` \| `not_found` |
| `locator` | String | JSON path like `$.DATA.referenceId` or null |
| `reasoningNote` | String | Human-readable explanation from LLM |

---

### AiFieldResolution (LLM response schema)

This is the structured JSON the LLM is asked to return — it is NOT persisted separately but mapped to `AiStepResult.resolvedFields`.

```json
{
  "resolvedFields": [
    { "fieldName": "string", "value": "string|null", "source": "previous_response|extracted|default", "locator": "$.path or null", "reasoningNote": "string" }
  ],
  "requestBody": {},
  "headerOverrides": {},
  "queryParamOverrides": {}
}
```

---

## Enums / Constants

```
AiWorkflow.status:     active | archived
AiWorkflowRun.status:  passed | failed | partial | llm_unavailable | error
AiStepResult.status:   pass | fail | skipped | error
ResolvedField.source:  previous_response | extracted | default | not_found
```

---

## Storage Layout

```
data/
├── ai-workflows/
│   ├── aiwf-a1b2c3d4.json
│   └── aiwf-e5f6g7h8.json
└── ai-workflow-runs/
    ├── aiwfrun-12345678/
    │   └── results.json
    └── aiwfrun-abcdef01/
        └── results.json
```

---

## Variable Accumulation During Execution

Each step's response is accumulated into a `runContext` map:

```
runContext = {
  "step1.response": "<full JSON string of step 1 response>",
  "step2.response": "<full JSON string of step 2 response>",
  ...
}
```

This full context is provided to the LLM for each subsequent step, enabling multi-hop data tracing.

---

## Reused Models (no changes)

- `RequestSnapshot` (from 003): method, url, headers, body
- `NormalizedSpec` + `EndpointSpec` + `ParameterSpec`: read-only for LLM context
- `AuthConfig`: global auth + headers applied to every step
