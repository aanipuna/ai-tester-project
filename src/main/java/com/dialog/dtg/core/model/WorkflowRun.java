package com.dialog.dtg.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WorkflowRun {

    private String workflowRunId;
    private String workflowId;
    private String workflowName;
    private Instant startedAt;
    private Instant finishedAt;
    private String status; // passed | failed | partial | error
    private List<StepResult> stepResults = new ArrayList<>();

    public String getWorkflowRunId() { return workflowRunId; }
    public void setWorkflowRunId(String workflowRunId) { this.workflowRunId = workflowRunId; }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<StepResult> getStepResults() { return stepResults; }
    public void setStepResults(List<StepResult> stepResults) { this.stepResults = stepResults != null ? stepResults : new ArrayList<>(); }
}
