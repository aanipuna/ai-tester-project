# Quickstart: API Test Agent Validation

## Purpose
Validate end-to-end behavior for CLI and web workflows using the same core services and file-based persistence.

## Prerequisites
- Java 17 or higher installed
- Maven 3.9 or higher installed
- Anthropic API key available for scenario generation and narrative reporting
- Network access to a reachable sample API

## Setup
1. Build the application JAR:
   - mvn clean package
2. Prepare environment variable for LLM calls:
   - set ANTHROPIC_API_KEY=your_key_here
3. Ensure data directory exists or is creatable:
   - default ./data (override through application configuration)

## Validation Scenarios

### Scenario A: CLI generation and execution
1. Generate a plan from a sample API spec:
   - java -jar target/api-test-agent.jar generate --spec ./samples/petstore-openapi.yaml
2. Confirm artifacts:
   - one spec file under data/specs
   - one plan file under data/plans
3. Execute plan:
   - java -jar target/api-test-agent.jar run --plan data/plans/<plan-id>.json
4. Confirm run artifacts:
   - data/runs/<run-id>/results.json created
5. Generate report:
   - java -jar target/api-test-agent.jar report --run <run-id>
6. Confirm report files:
   - data/runs/<run-id>/report.md
   - data/runs/<run-id>/report.html

Expected outcome:
- CLI outputs pass/fail summary and non-zero exit on failed assertions.
- Persisted files include schemaVersion and valid JSON structures.

### Scenario B: Web UI end-to-end
1. Start without CLI args:
   - java -jar target/api-test-agent.jar
2. Open local UI at http://localhost:8080
3. Upload sample spec, generate scenarios, edit one test case, then save.
4. Execute plan from UI and observe incremental result updates.
5. Open generated report and verify download links for HTML and Markdown.

Expected outcome:
- UI actions persist to same data directory used by CLI.
- Run and report artifacts match execution selected in UI.

### Scenario C: Import pre-authored plan
1. Import a user-authored plan JSON matching contract schema.
2. Run imported plan via CLI and UI.

Expected outcome:
- No generation call is required for imported plans.
- Imported plan is editable with CRUD operations.

### Scenario D: Failure and recovery checks
1. Force endpoint timeout/unreachable host in one test case.
2. Verify run records failure evidence without aborting unrelated cases.
3. Simulate malformed JSON persistence file for a plan.
4. Restart and confirm user receives recovery guidance and data safety behavior.

Expected outcome:
- Errors are explicit and actionable.
- Corrupted files do not silently overwrite existing good state.

## Contract References
- REST contract: see contracts/rest-api.yaml
- CLI contract: see contracts/cli-commands.md
- Data model and states: see data-model.md

## Acceptance Mapping
- User Story 1 validated by Scenario A and C.
- User Story 2 validated by Scenario B.
- User Story 3 validated by Scenario A and D.

## Implementation Verification Notes

- Code artifacts for CLI, web controllers, core services, persistence stores, templates, and report outputs have been implemented.
- Static diagnostics show no Java language-server errors in main and test source trees.
- Full `mvn test` could not be executed in this environment because Maven is not installed on PATH.
- To finalize runtime validation locally, install Maven and run:
   - `mvn clean test`
   - `mvn clean package`
