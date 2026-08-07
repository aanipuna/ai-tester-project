# Research: AI-Driven API Workflow Chaining

**Branch**: `004-ai-workflow-chaining`

---

## 1. LLM Prompt Design for Field Resolution

**Decision**: Use a single-shot structured prompt per step transition that gives the AI:
1. The previous step's full response body (JSON)
2. The next step's endpoint definition from the spec (method, path, parameter names, types, locations, required flags)
3. All previously extracted values from earlier steps in this run (accumulated context)

**Prompt schema**:
```
You are an API test automation assistant. Given:
1. The response from the previous API call
2. The next API endpoint's parameter specification
3. Previously extracted values from this run

Determine the appropriate values for each parameter of the next API call.

Previous response:
<json>

Next endpoint: {{method}} {{path}}
Parameters:
<parameter list from NormalizedSpec>

Previously extracted values:
<accumulated key-value map>

Return ONLY valid JSON matching:
{
  "resolvedFields": [
    { "fieldName": "string", "value": "string|null", "source": "previous_response|extracted|default", "locator": "$.path.to.value or null", "reasoningNote": "string" }
  ],
  "requestBody": { ...resolved body object... },
  "headerOverrides": { "key": "value" },
  "queryParamOverrides": { "key": "value" }
}
```

**Rationale**: Single-shot structured output is simpler than multi-turn conversation. The `requestBody` field gives a complete resolved request body ready to send. `resolvedFields` provides per-field traceability for the run result.

**Alternatives considered**: Multi-turn conversation (more tokens, more complex state management); rule-based JSONPath extraction with LLM fallback (defeats the purpose of AI automation).

---

## 2. Reuse of Existing Infrastructure

**Decision**: Reuse everything from the existing codebase:

| Component | Reuse |
|-----------|-------|
| `ChatClient` | Spring AI ChatClient — same as ScenarioGeneratorService |
| `SpecStore` + `NormalizedSpec` + `EndpointSpec` + `ParameterSpec` | Spec data for LLM context |
| `AuthConfigStore` | Global auth + headers applied to every step |
| `WebClient` | Same HTTP execution as ExecutorService and WorkflowExecutorService |
| `Ids.java` | New `nextAiWorkflowId()`, `nextAiWorkflowRunId()` |
| `DataPathProperties` | New `aiWorkflowsDir()`, `aiWorkflowRunsDir()` |
| `JsonRepositorySupport` | Base class for new stores |
| Page nav (workflows.html etc.) | Already has Workflows link |

**Zero new frameworks or libraries required.**

---

## 3. Data Flow Per Step

```
Step N runs:
  1. Build request using spec-defined path + global auth + any prior AI-resolved overrides
  2. Send HTTP request via WebClient
  3. Receive response body (string JSON)
  4. Accumulate response into context map: { "stepN.response": "<json string>" }

Before Step N+1:
  1. Build LLM prompt: prior response + step N+1 spec params + accumulated context
  2. Call ChatClient → get resolvedFields + requestBody + headerOverrides
  3. Build step N+1 request using resolved values
  4. Store AiStepResult with reasoning notes
```

---

## 4. Storage Strategy

**Decision**: File-based JSON in `data/ai-workflows/` and `data/ai-workflow-runs/` — same pattern as all existing stores.

No new infrastructure. Compatible with existing data backup and cleanup logic.

---

## 5. Integration with Existing Workflow Page

**Decision**: Add a second section to the existing `/workflows` page — "AI Workflows" below the manual "My Workflows" grid. New create page at `/ai-workflows/new`, detail at `/ai-workflows/{id}`, run result at `/ai-workflow-runs/{runId}`.

This keeps everything in one "Workflows" navigation entry consistent with constitution principle III (UX consistency).

---

## 6. REST API Surface

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/ai-workflows` | Create AI workflow |
| `GET` | `/api/ai-workflows` | List all AI workflows |
| `GET` | `/api/ai-workflows/{id}` | Get AI workflow |
| `PUT` | `/api/ai-workflows/{id}` | Update AI workflow |
| `DELETE` | `/api/ai-workflows/{id}` | Delete AI workflow |
| `POST` | `/api/ai-workflows/{id}/runs` | Execute AI workflow |
| `GET` | `/api/ai-workflow-runs` | List all AI workflow runs |
| `GET` | `/api/ai-workflow-runs/{runId}` | Get AI workflow run result |

---

## 7. LLM Fallback Strategy

- If `ChatClient` is null (no API key): run fails with `status = llm_unavailable`, all steps show `skipped`, run result explains that AI is not configured.
- If LLM returns malformed JSON: log warning, attempt best-effort parse, fall back to empty body + null overrides with a warning recorded in `AiStepResult.failureReason`.
- If LLM returns null for a required field: inject null/empty, record warning in `reasoningNote`.

---

## All Unknowns Resolved

No NEEDS CLARIFICATION items. All reuse patterns confirmed in existing codebase.
