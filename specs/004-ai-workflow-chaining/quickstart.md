# Quickstart Validation Guide: AI-Driven API Workflow Chaining

**Branch**: `004-ai-workflow-chaining`

---

## Prerequisites

1. App running at http://localhost:8080
2. FinTech Postman collection ingested (spec with endpoints visible)
3. **Anthropic API key configured** in Settings → Global Auth (or in `start.bat` as `ANTHROPIC_API_KEY`)

---

## Scenario 1: Create a 2-Step AI Workflow

### Steps
1. Navigate to http://localhost:8080/workflows
2. Click **+ New AI Workflow** (in the AI Workflows section)
3. Select spec: `FinTech_HUB - STG`
4. Add Step 1: select endpoint `POST /api/auth/otp/request`
5. Add Step 2: select endpoint `POST /api/auth/otp/verify`
6. Enter name: `OTP Chain Test`
7. Click **Save**

### Expected
- Workflow saved with 2 steps
- Appears in the AI Workflows list
- No extraction rules or injection rules to configure — just endpoint selection

---

## Scenario 2: Run the AI Workflow and Inspect AI Reasoning

### Steps
1. Open `OTP Chain Test`
2. Click **▶ Run AI Workflow**
3. Wait for completion (up to ~20 seconds including LLM calls)

### Expected
Run result shows:
- **Step 1** (Request OTP): actual request body with AI-chosen values (e.g. a sample mobile number), HTTP response
- **Step 2** (Verify OTP): AI resolved `referenceId` from step 1 response, reasoning note shows `"Extracted referenceId from step 1 response.DATA.referenceId"`
- Per-field `resolvedFields` list visible for each step

---

## Scenario 3: AI Handles Missing LLM Key Gracefully

### Steps
1. Clear the Anthropic API key from `start.bat`
2. Restart the app
3. Try running any AI workflow

### Expected
- Run status: `llm_unavailable`
- All steps show `skipped`
- Error message: "AI model not configured. Set ANTHROPIC_API_KEY to enable AI workflow chaining."

---

## Scenario 4: Edit AI Workflow — Reorder Steps

### Steps
1. Open `OTP Chain Test`, click Edit
2. Add Step 3: select `POST /api/auth/oauth/token`
3. Move step 2 up (if needed)
4. Save

### Expected
- Workflow now has 3 steps in the new order
- Next run uses the updated step order

---

## Verification Commands (API)

```powershell
# Create AI workflow
$specs = Invoke-RestMethod -Uri "http://localhost:8080/api/specs"
$specId = $specs[0].specId
$endpoint1 = $specs[0].endpoints[0].endpointId
$endpoint2 = $specs[0].endpoints[2].endpointId

$wf = Invoke-RestMethod -Uri "http://localhost:8080/api/ai-workflows" -Method POST `
  -ContentType "application/json" `
  -Body "{`"name`":`"Test AI Chain`",`"specId`":`"$specId`",`"steps`":[{`"name`":`"Step1`",`"endpointId`":`"$endpoint1`"},{`"name`":`"Step2`",`"endpointId`":`"$endpoint2`"}]}"
Write-Host "Created: $($wf.workflowId)"

# Run it
$run = Invoke-RestMethod -Uri "http://localhost:8080/api/ai-workflows/$($wf.workflowId)/runs" -Method POST
Write-Host "Run status: $($run.status)"
$run.stepResults | ForEach-Object { Write-Host "  $($_.stepName): $($_.status) | $($_.resolvedFields.Count) fields resolved" }
```

---

## UI Consistency Checklist

- [ ] AI Workflows section appears on `/workflows` page below manual workflows
- [ ] Same card layout and badge styles as manual workflows
- [ ] Dark mode works on all new pages (ai-workflow-edit, ai-workflow-detail, ai-workflow-run-detail)
- [ ] Workflows nav item navigates to the combined page
- [ ] All existing pages still function — no regressions
