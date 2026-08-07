<!--
Sync Impact Report
- Version change: 1.0.0 → 1.1.0
- Modified principles:
	- II. Testing Is a Release Gate — expanded to include API automation test requirements
- Added sections:
	- VI. API Automation Quality Standards (new principle)
- Removed sections:
	- None
- Follow-up TODOs:
	- None
-->
# AI Tester Project Constitution

## Core Principles

### I. Code Quality Is Mandatory
All production code MUST meet defined quality baselines before merge: clear naming,
single-responsibility structure, explicit error handling, and zero unresolved lint errors in
changed files. Public interfaces MUST include concise documentation of behavior and failure
modes. Pull requests that introduce avoidable complexity or dead code MUST be rejected.
Rationale: high code quality reduces defect rates, review overhead, and long-term maintenance
cost.

### II. Testing Is a Release Gate
Every functional change MUST include automated tests at the appropriate level (unit,
integration, or end-to-end) and MUST prove the intended behavior and key failure paths.
Bug fixes MUST include a regression test that fails before the fix and passes after it.
Mainline builds MUST remain green; no release is permitted with failing or skipped required
tests. New API endpoints or changes to existing ones MUST include at minimum one positive,
one negative, and one auth test case in the automated test plan.
Rationale: test enforcement protects reliability and prevents recurring regressions.

### III. User Experience Must Be Consistent
User-facing behavior MUST remain consistent across equivalent workflows, including language,
layout patterns, input validation behavior, error messaging tone, and accessibility semantics.
New UI or API interaction patterns MUST align with existing project conventions or include an
approved migration plan. Rationale: consistency reduces user confusion, support burden, and
adoption friction.

### IV. Performance Budgets Are Enforceable
Features MUST define measurable performance expectations before merge, including latency,
resource usage, and responsiveness targets relevant to the component. Changes that exceed
approved budgets MUST be optimized or explicitly waived with documented justification and
expiration date. Performance-sensitive paths MUST include benchmark or profiling evidence in
the review artifact. Rationale: explicit budgets prevent gradual performance degradation.

### V. Simplicity and Maintainability First
Solutions MUST prefer the simplest design that satisfies current requirements, with extension
points added only when justified by concrete near-term needs. Duplicate logic MUST be reduced
through well-scoped reuse, and architectural decisions MUST favor readability over cleverness.
Rationale: simple systems are easier to verify, evolve, and operate safely.

### VI. API Automation Quality Standards
All new features added to the existing project MUST be accompanied by a corresponding test
plan in the API test agent. Test plans MUST cover:
- At least one **positive** case with realistic sample parameter values.
- At least one **negative** case per required parameter (missing or malformed value).
- At least one **auth** case if the endpoint requires authentication.
- At least one **boundary** case for string/numeric fields where applicable.

Generated test descriptions MUST be human-readable and include the parameter names under
test and the expected outcome (e.g. "Missing `mobile_number` — expect 400 Bad Request").
Test execution results MUST be persisted and reportable in both HTML and Markdown formats.
Global authentication headers MUST be configured in Settings before running test plans
against protected environments.
Rationale: consistent automation coverage prevents regression and accelerates confident
delivery of new features.

## Operational Standards

- Tooling and automation MUST run in a repeatable way in local and CI environments.
- Definitions of done MUST include documentation updates for any changed behavior,
	configuration, or operational procedure.
- Accessibility, observability, and error diagnostics MUST be preserved or improved by default
	in user-impacting changes.
- Security and privacy requirements take precedence over convenience in implementation choices.
- API keys and secrets MUST NOT be committed to version control; use the `start.bat` startup
	script (gitignored) or environment variables for runtime injection.

## Delivery Workflow and Quality Gates

- Every pull request MUST include: purpose summary, scope boundaries, validation evidence,
	and risk notes.
- Reviewers MUST verify compliance with all core principles and block merges on violations.
- CI MUST enforce linting, required tests, and policy checks before merge.
- Urgent exceptions MAY be approved only with documented owner, rationale, and remediation
	deadline.
- New API endpoints MUST have a corresponding test spec generated via the API Test Agent
	before the feature is considered done.

## Governance

This constitution is the authoritative engineering policy for this repository. When other
guidance conflicts with this document, this constitution takes precedence.

Amendments require a documented proposal, reviewer approval, and an impact statement covering
affected workflows, migration needs, and enforcement updates.

Versioning policy:
- MAJOR: incompatible governance changes or principle removals/redefinitions.
- MINOR: new principle/section or materially expanded guidance.
- PATCH: clarifications, wording improvements, and non-semantic refinements.

Compliance review expectations:
- Every review cycle MUST include an explicit constitution compliance check.
- At least once per quarter, maintainers SHOULD review this constitution for continued
	relevance and enforceability.

**Version**: 1.1.0 | **Ratified**: 2026-08-06 | **Last Amended**: 2026-08-07
# AI Tester Project Constitution

## Core Principles

### I. Code Quality Is Mandatory
All production code MUST meet defined quality baselines before merge: clear naming,
single-responsibility structure, explicit error handling, and zero unresolved lint errors in
changed files. Public interfaces MUST include concise documentation of behavior and failure
modes. Pull requests that introduce avoidable complexity or dead code MUST be rejected.
Rationale: high code quality reduces defect rates, review overhead, and long-term maintenance
cost.

### II. Testing Is a Release Gate
Every functional change MUST include automated tests at the appropriate level (unit,
integration, or end-to-end) and MUST prove the intended behavior and key failure paths.
Bug fixes MUST include a regression test that fails before the fix and passes after it.
Mainline builds MUST remain green; no release is permitted with failing or skipped required
tests. Rationale: test enforcement protects reliability and prevents recurring regressions.

### III. User Experience Must Be Consistent
User-facing behavior MUST remain consistent across equivalent workflows, including language,
layout patterns, input validation behavior, error messaging tone, and accessibility semantics.
New UI or API interaction patterns MUST align with existing project conventions or include an
approved migration plan. Rationale: consistency reduces user confusion, support burden, and
adoption friction.

### IV. Performance Budgets Are Enforceable
Features MUST define measurable performance expectations before merge, including latency,
resource usage, and responsiveness targets relevant to the component. Changes that exceed
approved budgets MUST be optimized or explicitly waived with documented justification and
expiration date. Performance-sensitive paths MUST include benchmark or profiling evidence in
the review artifact. Rationale: explicit budgets prevent gradual performance degradation.

### V. Simplicity and Maintainability First
Solutions MUST prefer the simplest design that satisfies current requirements, with extension
points added only when justified by concrete near-term needs. Duplicate logic MUST be reduced
through well-scoped reuse, and architectural decisions MUST favor readability over cleverness.
Rationale: simple systems are easier to verify, evolve, and operate safely.

## Operational Standards

- Tooling and automation MUST run in a repeatable way in local and CI environments.
- Definitions of done MUST include documentation updates for any changed behavior,
	configuration, or operational procedure.
- Accessibility, observability, and error diagnostics MUST be preserved or improved by default
	in user-impacting changes.
- Security and privacy requirements take precedence over convenience in implementation choices.

## Delivery Workflow and Quality Gates

- Every pull request MUST include: purpose summary, scope boundaries, validation evidence,
	and risk notes.
- Reviewers MUST verify compliance with all core principles and block merges on violations.
- CI MUST enforce linting, required tests, and policy checks before merge.
- Urgent exceptions MAY be approved only with documented owner, rationale, and remediation
	deadline.

## Governance

This constitution is the authoritative engineering policy for this repository. When other
guidance conflicts with this document, this constitution takes precedence.

Amendments require a documented proposal, reviewer approval, and an impact statement covering
affected workflows, migration needs, and enforcement updates.

Versioning policy:
- MAJOR: incompatible governance changes or principle removals/redefinitions.
- MINOR: new principle/section or materially expanded guidance.
- PATCH: clarifications, wording improvements, and non-semantic refinements.

Compliance review expectations:
- Every review cycle MUST include an explicit constitution compliance check.
- At least once per quarter, maintainers SHOULD review this constitution for continued
	relevance and enforceability.

**Version**: 1.0.0 | **Ratified**: 2026-08-06 | **Last Amended**: 2026-08-06
