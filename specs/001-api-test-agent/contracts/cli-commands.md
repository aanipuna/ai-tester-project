# CLI Contract: API Test Agent

## Command Entry
- Executable: java -jar api-test-agent.jar
- Mode detection:
  - if first non-option token is generate, run, or report, app runs in CLI mode
  - otherwise app starts local web server mode

## Command: generate
Generate a test plan from source spec input.

Syntax:
- java -jar api-test-agent.jar generate --spec <path>
- java -jar api-test-agent.jar generate --postman <path>
- java -jar api-test-agent.jar generate --manual <path-to-manual-spec-json>
- optional: --data-dir <path>

Inputs:
- exactly one source option is required
- source file must exist and be readable

Outputs:
- stdout summary with created specId and planId
- persisted files:
  - data/specs/{specId}.json
  - data/plans/{planId}.json

Exit codes:
- 0 success
- 2 validation error (bad args, invalid source)
- 3 generation error (LLM call or output validation failure)
- 4 persistence error

## Command: run
Execute a saved or imported test plan.

Syntax:
- java -jar api-test-agent.jar run --plan <path-or-plan-id>
- optional: --data-dir <path>
- optional: --only <test-case-id-list>

Inputs:
- plan must resolve to valid plan JSON schema
- at least one enabled test case required

Outputs:
- stdout pass/fail summary and runId
- persisted file: data/runs/{runId}/results.json

Exit codes:
- 0 all executed tests passed
- 1 one or more tests failed
- 2 validation error
- 4 execution or persistence error

## Command: report
Generate markdown and html report from a run.

Syntax:
- java -jar api-test-agent.jar report --run <run-id>
- optional: --data-dir <path>

Inputs:
- run directory and results.json must exist

Outputs:
- stdout summary with report paths
- persisted files:
  - data/runs/{runId}/report.md
  - data/runs/{runId}/report.html

Exit codes:
- 0 success
- 2 validation error
- 3 narrative generation error
- 4 report persistence error

## Common Rules
- Every persisted output includes schemaVersion field.
- Sensitive secrets are masked in default stdout output.
- Error messages are human-readable and actionable.
