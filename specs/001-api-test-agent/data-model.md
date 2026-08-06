# Data Model: API Test Agent

## Overview
The system uses file-based JSON documents with explicit schemaVersion fields. Data is organized under a configurable data directory with separate domains for specs, plans, and runs.

## Entity: NormalizedSpec
- Description: Canonical internal representation of API definition regardless of input source (OpenAPI, Postman, manual entry).
- Storage: data/specs/{specId}.json
- Key fields:
  - schemaVersion: string
  - specId: string
  - sourceType: enum(openapi, postman, manual)
  - name: string
  - baseUrl: string
  - endpoints: Endpoint[]
  - importedAt: timestamp
  - tags: string[]
- Validation rules:
  - specId must be unique in local workspace.
  - endpoints must contain at least one operation.
  - each endpoint must define method and path.
  - authType must be one of none, bearer, basic, apiKey, oauth2.

## Entity: Endpoint
- Description: Testable API operation definition under a normalized spec.
- Key fields:
  - endpointId: string
  - method: enum(GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS)
  - path: string
  - authType: enum(none, bearer, basic, apiKey, oauth2)
  - parameters: Parameter[]
  - requestBodySchema: object|null
  - expectedSuccessStatus: integer
  - expectedResponseSchema: object|null
- Validation rules:
  - path must begin with /.
  - expectedSuccessStatus must be valid HTTP code.

## Entity: Parameter
- Description: Canonical parameter metadata used for generation and execution.
- Key fields:
  - name: string
  - location: enum(query, path, header, body)
  - dataType: string
  - required: boolean
  - constraints: object

## Entity: TestPlan
- Description: Editable set of generated or imported test cases linked to one normalized endpoint/spec.
- Storage: data/plans/{planId}.json
- Key fields:
  - schemaVersion: string
  - planId: string
  - sourceSpecId: string
  - sourceEndpoint: string
  - createdBy: enum(generated, imported, manual)
  - createdAt: timestamp
  - updatedAt: timestamp
  - status: enum(active, archived)
  - testCases: TestCase[]
- Validation rules:
  - planId unique.
  - at least one enabled test case required for execution.
  - sourceSpecId must exist unless plan is fully imported standalone.

## Entity: TestCase
- Description: Individual executable test definition in a plan.
- Key fields:
  - id: string
  - category: enum(positive, negative, boundary, auth, idempotency)
  - enabled: boolean
  - description: string
  - request: RequestSpec
  - expectedStatus: integer
  - expectedBehavior: string
  - timeoutMs: integer
- Validation rules:
  - id unique within plan.
  - expectedStatus between 100 and 599.
  - request.method and request.path required.

## Entity: RequestSpec
- Description: Request payload used by executor.
- Key fields:
  - method: string
  - path: string
  - headers: map<string,string>
  - queryParams: map<string,string|number|boolean>
  - body: object|null

## Entity: TestRun
- Description: One execution instance for a plan.
- Storage: data/runs/{runId}/results.json and report outputs
- Key fields:
  - schemaVersion: string
  - runId: string
  - planId: string
  - startedAt: timestamp
  - finishedAt: timestamp
  - triggeredBy: enum(cli, web)
  - summary: RunSummary
  - results: CaseResult[]
- Validation rules:
  - runId unique.
  - finishedAt must be >= startedAt once complete.

## Entity: CaseResult
- Description: Captured result for each executed test case.
- Key fields:
  - testCaseId: string
  - category: string
  - status: enum(pass, fail, error, skipped)
  - httpStatus: integer|null
  - responseTimeMs: integer|null
  - assertionResults: AssertionResult[]
  - responseSnapshot: object|string|null
  - failureReason: string|null
- Validation rules:
  - status determines required fields (e.g., fail requires failureReason).

## Entity: AssertionResult
- Description: Fine-grained assertion evaluation output.
- Key fields:
  - assertionType: enum(status, header, body, latency, behavior)
  - expected: string|number|object
  - actual: string|number|object|null
  - passed: boolean
  - message: string

## Entity: RunReport
- Description: Human-consumable report artifacts generated from TestRun.
- Storage: data/runs/{runId}/report.md and report.html
- Key fields:
  - runId: string
  - generatedAt: timestamp
  - metrics: ReportMetrics
  - narrativeSummary: string

## Relationships
- NormalizedSpec 1..* Endpoint
- TestPlan 1..* TestCase
- TestRun 1..* CaseResult
- CaseResult 1..* AssertionResult
- NormalizedSpec 1..* TestPlan (logical source relation)
- TestPlan 1..* TestRun
- TestRun 1..1 RunReport

## State Transitions

### TestPlan.status
- active -> archived
- archived -> active
- archived plans are read-only for execution unless reactivated.

### TestRun lifecycle
- queued -> running -> completed
- queued -> running -> failed
- running -> canceled

### TestCase.enabled
- true <-> false
- disabled test cases are excluded from execution but preserved in plan file.
