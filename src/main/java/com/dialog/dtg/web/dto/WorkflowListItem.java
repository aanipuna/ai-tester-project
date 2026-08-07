package com.dialog.dtg.web.dto;

public class WorkflowListItem {

    private String workflowId;
    private String name;
    private String description;
    private int stepCount;
    private String status;
    private String updatedAt;

    public WorkflowListItem() {}

    public WorkflowListItem(String workflowId, String name, String description, int stepCount, String status, String updatedAt) {
        this.workflowId = workflowId;
        this.name = name;
        this.description = description;
        this.stepCount = stepCount;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStepCount() { return stepCount; }
    public void setStepCount(int stepCount) { this.stepCount = stepCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
