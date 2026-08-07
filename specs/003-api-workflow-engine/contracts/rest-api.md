# REST API Contract: API Workflow Engine

**Branch**: `003-api-workflow-engine`
**Base path**: `/api`

All requests and responses use `Content-Type: application/json` unless noted.

---

## Workflow CRUD

### `POST /api/workflows` — Create Workflow

**Request body**:
```json
{
  "name": "OTP Auth Flow",
  "description": "Full OTP login → token extraction → protected call",
  "baseUrl": "https://stg.finpal.lk",
  "variables": {
    "testMobile": "+94771234567"
  },
  "steps": [
    {
      "name": "Request OTP",
      "method": "POST",
      "path": "/api/auth/otp/request",
      "body": "{\"mobile_number\": \"{{testMobile}}\"}",
      "extractions": [
        { "variableName": "referenceId", "source": "BODY", "locator": "$.DATA.referenceId" }
      ]
    },
    {
      "name": "Verify OTP",
      "method": "POST",
      "path": "/api/auth/otp/verify",
      "body": "{\"otp\": \"123456\"}",
      "injections": [
        { "target": "BODY_FIELD", "targetKey": "referenceId", "variableRef": "{{step1.referenceId}}" }
      ],
      "conditions": [
        { "sourceStepId": "step1-id", "source": "STATUS", "operator": "EQ", "expectedValue": "200" }
      ]
    }
  ]
}
```

**Response `201 Created`**:
```json
{ "workflowId": "wf-a1b2c3d4", "name": "OTP Auth Flow", "status": "active", "steps": [...] }
```

**Errors**: `400` invalid body / missing required fields.

---

### `GET /api/workflows` — List All Workflows

**Response `200 OK`**:
```json
[
  { "workflowId": "wf-a1b2c3d4", "name": "OTP Auth Flow", "status": "active", "stepCount": 2, "updatedAt": "..." },
  { "workflowId": "wf-e5f6g7h8", "name": "Profile Update Flow", "status": "active", "stepCount": 4, "updatedAt": "..." }
]
```

---

### `GET /api/workflows/{workflowId}` — Get Workflow

**Response `200 OK`**: Full workflow object including all steps, rules, and conditions.

**Errors**: `404` workflow not found.

---

### `PUT /api/workflows/{workflowId}` — Update Workflow (full replace)

**Request body**: Same schema as `POST /api/workflows`.

**Response `200 OK`**: Updated workflow object.

**Errors**: `400` invalid body, `404` not found.

---

### `DELETE /api/workflows/{workflowId}` — Delete Workflow

**Response `204 No Content`**.

**Errors**: `404` not found.

---

## Workflow Execution

### `POST /api/workflows/{workflowId}/runs` — Execute Workflow

Runs all steps sequentially, applying data extraction, injection, and conditions.

**Request body**: Empty (uses workflow's own configuration).

**Response `200 OK`** (synchronous — waits for all steps to complete):
```json
{
  "workflowRunId": "wfrun-12345678",
  "workflowId": "wf-a1b2c3d4",
  "workflowName": "OTP Auth Flow",
  "startedAt": "2026-08-07T10:00:00Z",
  "finishedAt": "2026-08-07T10:00:05Z",
  "status": "passed",
  "stepResults": [
    {
      "stepId": "step-001",
      "stepName": "Request OTP",
      "status": "pass",
      "requestSent": { "method": "POST", "url": "https://stg.finpal.lk/api/auth/otp/request", "headers": {}, "body": "{\"mobile_number\":\"+94771234567\"}" },
      "httpStatus": 200,
      "responseBody": "{\"STATUS\":\"SUCCESS\",\"DATA\":{\"referenceId\":\"abc-123\"}}",
      "responseTimeMs": 342,
      "extractedValues": { "step1.referenceId": "abc-123" },
      "conditionResults": []
    },
    {
      "stepId": "step-002",
      "stepName": "Verify OTP",
      "status": "pass",
      "requestSent": { "method": "POST", "url": "https://stg.finpal.lk/api/auth/otp/verify", "headers": {}, "body": "{\"otp\":\"123456\",\"referenceId\":\"abc-123\"}" },
      "httpStatus": 200,
      "responseBody": "{\"STATUS\":\"SUCCESS\"}",
      "responseTimeMs": 281,
      "extractedValues": {},
      "conditionResults": [{ "conditionIndex": 0, "operator": "EQ", "actualValue": "200", "expectedValue": "200", "passed": true }]
    }
  ]
}
```

**Errors**: `404` workflow not found, `500` unexpected execution error.

---

## Workflow Run History

### `GET /api/workflow-runs` — List All Runs

**Response `200 OK`**:
```json
[
  { "workflowRunId": "wfrun-12345678", "workflowId": "wf-a1b2c3d4", "workflowName": "OTP Auth Flow", "status": "passed", "startedAt": "...", "stepCount": 2 }
]
```

### `GET /api/workflow-runs/{workflowRunId}` — Get Run Details

**Response `200 OK`**: Full WorkflowRun object with all StepResults.

**Errors**: `404` not found.

---

## Page Routes (Thymeleaf)

| Route | Template | Description |
|-------|----------|-------------|
| `GET /workflows` | `workflows.html` | Workflow list + create button |
| `GET /workflows/{workflowId}` | `workflow-detail.html` | View + edit + run workflow |
| `GET /workflow-runs/{runId}` | `workflow-run-detail.html` | View run result with step-by-step detail |

---

## UI POST Routes (form submits)

| Route | Action |
|-------|--------|
| `POST /ui/workflows/{workflowId}/delete` | Delete workflow, redirect to `/workflows` |
| `POST /ui/workflows/{workflowId}/run` | Execute workflow, redirect to run result |
