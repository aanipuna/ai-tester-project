package com.dialog.dtg.core.model;

import java.time.Instant;
import java.util.List;

public class RunReport {

    private String runId;
    private Instant generatedAt;
    private ReportMetrics metrics;
    private String narrativeSummary;
    private List<CaseResult> results;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public ReportMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(ReportMetrics metrics) {
        this.metrics = metrics;
    }

    public String getNarrativeSummary() {
        return narrativeSummary;
    }

    public void setNarrativeSummary(String narrativeSummary) {
        this.narrativeSummary = narrativeSummary;
    }

    public List<CaseResult> getResults() {
        return results;
    }

    public void setResults(List<CaseResult> results) {
        this.results = results;
    }
}
