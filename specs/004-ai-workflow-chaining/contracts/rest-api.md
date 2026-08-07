# REST API Contract: AI-Driven API Workflow Chaining

**Branch**: `004-ai-workflow-chaining`

---

## AI Workflow CRUD

### `POST /api/ai-workflows` — Create AI Workflow

**Request**:
```json
{
  "name": "FinTech OTP → Token → Profile",
  "description": "AI-chained 3-step auth flow",
  "specId": "spec-952b36e5",
  "steps": [
    { "name": "Request OTP",    "endpointId": "POST-api-auth-otp-request" },
    { "name": "Verify OTP",     "endpointId": "POST-api-auth-otp-verify" },
    { "name": "Get OAuth Token","endpointId": "POST-api-auth-oauth-token", "expectedStatus": 200 }
  ]
}
```

**Response `201`**:
```json
{ "workflowId": "aiwf-a1b2c3d4", "name": "FinTech OTP → Token → Profile", "specId": "spec-952b36e5", "status": "active", "steps": [...] }
```

**Errors**: `400` invalid body, `404` spec not found or endpoint not in spec.

---

### `GET /api/ai-workflows` — List (slim)

**Response `200`**:
```json
[{ "workflowId": "aiwf-a1b2c3d4", "name": "...", "specId": "...", "stepCount": 3, "status": "active", "updatedAt": "..." }]
```

### `GET /api/ai-workflows/{id}` — Full detail

**Response `200`**: Full `AiWorkflow` with all steps (endpoint IDs + names).

### `PUT /api/ai-workflows/{id}` — Update (full replace)

Same schema as POST. Steps can be reordered, added, or removed.

### `DELETE /api/ai-workflows/{id}` — Delete

**Response `204 No Content`**. Also deletes all associated run history.

---

## AI Workflow Execution

### `POST /api/ai-workflows/{workflowId}/runs` — Execute

**Response `200`** (synchronous):
```json
{
  "runId": "aiwfrun-12345678",
  "workflowId": "aiwf-a1b2c3d4",
  "workflowName": "FinTech OTP → Token → Profile",
  "startedAt": "2026-08-07T10:00:00Z",
  "finishedAt": "2026-08-07T10:00:18Z",
  "status": "partial",
  "stepResults": [
    {
      "stepId": "aistep-001",
      "stepName": "Request OTP",
      "endpointRef": "POST /api/auth/otp/request",
      "resolvedRequest": {
        "method": "POST",
        "url": "https://stg.finpal.lk/api/auth/otp/request",
        "headers": { "Authorization": "Bearer <token>" },
        "body": "{\"mobile_number\":\"+94771234567\",\"device_id\":\"test-001\"}"
      },
      "httpStatus": 200,
      "responseBody": "{\"STATUS\":\"SUCCESS\",\"DATA\":{\"referenceId\":\"abc-123\"}}",
      "resolvedFields": [
        { "fieldName": "mobile_number", "value": "+94771234567", "source": "default", "locator": null, "reasoningNote": "Used default test value; no prior step provided mobile_number" },
        { "fieldName": "device_id", "value": "test-001", "source": "default", "locator": null, "reasoningNote": "Used default test device ID" }
      ],
      "status": "pass",
      "responseTimeMs": 412
    },
    {
      "stepId": "aistep-002",
      "stepName": "Verify OTP",
      "endpointRef": "POST /api/auth/otp/verify",
      "resolvedRequest": {
        "method": "POST",
        "url": "https://stg.finpal.lk/api/auth/otp/verify",
        "headers": {},
        "body": "{\"referenceId\":\"abc-123\",\"otp\":\"123456\"}"
      },
      "httpStatus": 401,
      "responseBody": "{\"error\":\"token_expired\"}",
      "resolvedFields": [
        { "fieldName": "referenceId", "value": "abc-123", "source": "previous_response", "locator": "$.DATA.referenceId", "reasoningNote": "Extracted referenceId from step 1 response.DATA.referenceId" },
        { "fieldName": "otp", "value": "123456", "source": "default", "locator": null, "reasoningNote": "OTP cannot be determined from prior response; used test default" }
      ],
      "status": "fail",
      "failureReason": "Expected 200 but got 401",
      "responseTimeMs": 280
    }
  ]
}
```

**Errors**: `404` workflow not found, `503` LLM unavailable (still returns a structured run result with `status: llm_unavailable`).

---

## Run History

### `GET /api/ai-workflow-runs` — List all runs (slim)

```json
[{ "runId": "aiwfrun-12345678", "workflowId": "...", "workflowName": "...", "status": "partial", "startedAt": "...", "stepCount": 3 }]
```

### `GET /api/ai-workflow-runs/{runId}` — Full run detail

Full `AiWorkflowRun` with all `AiStepResult` including `resolvedFields`.

---

## Page Routes (Thymeleaf)

| Route | Template | Description |
|-------|----------|-------------|
| `GET /ai-workflows/new` | `ai-workflow-edit.html` | Create new AI workflow (spec + endpoint picker) |
| `GET /ai-workflows/{id}` | `ai-workflow-detail.html` | View + run AI workflow |
| `GET /ai-workflow-runs/{runId}` | `ai-workflow-run-detail.html` | Run result with AI reasoning |

Also: `/workflows` page shows an "AI Workflows" section below the manual workflows grid.

## UI Form Routes

| Route | Action |
|-------|--------|
| `POST /ui/ai-workflows/{id}/delete` | Delete AI workflow + run history → redirect `/workflows` |
| `POST /ui/ai-workflows/{id}/run` | Execute AI workflow → redirect run result |
