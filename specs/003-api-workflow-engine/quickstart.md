# Quickstart Validation Guide: API Workflow Engine

**Branch**: `003-api-workflow-engine`

This guide describes how to validate that the API Workflow Engine is working correctly after implementation.

---

## Prerequisites

1. App running: `.\start.bat` (or `java -jar target/api-test-agent-0.1.0-SNAPSHOT.jar`)
2. FinTech Postman collection uploaded on the Specs page
3. Bearer token (if testing protected endpoints) configured in Settings → Global Auth

---

## Scenario 1: Create and Run a 2-Step Workflow

### Setup
Navigate to **http://localhost:8080/workflows** and click **New Workflow**.

### Steps
1. Enter name: `OTP Auth Flow`
2. Add **Step 1**: Method=`POST`, Path=`/api/auth/otp/request`, Body=`{"mobile_number": "{{testMobile}}"}`
3. In Step 1's Extractions, add: variable `referenceId`, source `BODY`, locator `$.DATA.referenceId`
4. In Workflow Variables, add: `testMobile = +94771234567`
5. Add **Step 2**: Method=`POST`, Path=`/api/auth/otp/verify`, Body=`{"otp": "123456"}`
6. In Step 2's Injections, add: target `BODY_FIELD`, key `referenceId`, variable `{{step1.referenceId}}`
7. Click **Save**

### Expected: Workflow saved
- Workflow appears in the list with "2 steps"
- Step 2 shows the injection rule referencing `step1.referenceId`

### Run the Workflow
Click **▶ Run** on the workflow.

### Expected: Run result
- Both steps show status (pass/fail depending on environment)
- Step 2's request body in the result shows `referenceId` populated with the value extracted from step 1's response
- Extracted values panel shows `step1.referenceId: <actual value>`

---

## Scenario 2: Condition — Skip Step if Previous Failed

### Steps
1. Create a workflow with 2 steps
2. On Step 2, add condition: source Step 1, source type `STATUS`, operator `EQ`, expected value `200`
3. Run the workflow against an endpoint that returns 401 on step 1

### Expected
- Step 1 completes with status fail (401 ≠ expected)
- Step 2 shows status `skipped` with reason "Condition not met: step1 STATUS EQ 200 (actual: 401)"
- Overall run status: `partial`

---

## Scenario 3: Edit Workflow — Reorder Steps

### Steps
1. Open an existing 3-step workflow in edit mode
2. Click the ↑ Up arrow on step 3 to move it to step 2 position
3. Click **Save Changes**

### Expected
- Workflow now has steps in the new order
- Any injection rules that referenced `{{step2.variable}}` are updated to `{{step3.variable}}` (old step 2 is now step 3)
- A broken-reference warning is shown if any injection references a variable from a step that no longer precedes it

---

## Scenario 4: Workflow Variables

### Steps
1. Define a workflow variable: `baseUsername = testuser@example.com`
2. Reference it in a step body field: `{"email": "{{baseUsername}}"}`
3. Run the workflow

### Expected
- Step request body in run result shows `{"email": "testuser@example.com"}`
- Changing the variable and re-running sends the updated value

---

## Scenario 5: Delete Workflow

### Steps
1. Click 🗑 Delete on a workflow
2. Confirm the dialog

### Expected
- Workflow removed from list
- Associated run history also removed

---

## Verification Commands (API)

```powershell
# Create workflow
Invoke-RestMethod -Uri "http://localhost:8080/api/workflows" -Method POST -ContentType "application/json" -Body '{"name":"Test Flow","steps":[{"name":"Step1","method":"GET","path":"/api/auth/product/list/v2"}]}'

# List workflows
Invoke-RestMethod -Uri "http://localhost:8080/api/workflows"

# Run workflow (replace wf-xxxxx with actual ID)
Invoke-RestMethod -Uri "http://localhost:8080/api/workflows/wf-xxxxx/runs" -Method POST

# Get run result
Invoke-RestMethod -Uri "http://localhost:8080/api/workflow-runs" | Select-Object -First 1 | ForEach-Object { Invoke-RestMethod -Uri "http://localhost:8080/api/workflow-runs/$($_.workflowRunId)" }
```

---

## UI Consistency Checklist

- [ ] Workflows nav item visible on all pages (Specs, Plans, Runs, Settings, Plan Detail, Run Detail)
- [ ] Workflows page uses the same card layout and color palette as Plans
- [ ] Dark mode toggles correctly on the Workflows page
- [ ] Theme preference persists across navigation to/from Workflows
- [ ] All existing pages (Specs, Plans, Runs, Settings) still function correctly with no regressions
- [ ] Workflow run results use the same badge styles (badge-ok, badge-fail, badge-off) as existing run results
