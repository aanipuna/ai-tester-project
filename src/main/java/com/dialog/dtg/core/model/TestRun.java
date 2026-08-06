package com.dialog.dtg.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestRun {

    private String schemaVersion;
    private String runId;
    private String planId;
    private Instant startedAt;
    private Instant finishedAt;
    private String triggeredBy;
    private RunSummary summary;
    private List<CaseResult> results = new ArrayList<>();

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public RunSummary getSummary() {
        return summary;
    }

    public void setSummary(RunSummary summary) {
        this.summary = summary;
    }

    public List<CaseResult> getResults() {
        return results;
    }

    public void setResults(List<CaseResult> results) {
        this.results = results;
    }
}
