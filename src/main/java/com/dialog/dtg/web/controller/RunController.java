package com.dialog.dtg.web.controller;

import com.dialog.dtg.core.model.RunReport;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.ReportStore;
import com.dialog.dtg.core.store.RunStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class RunController {

    private final WorkflowService workflowService;
    private final RunStore runStore;
    private final ReportStore reportStore;

    public RunController(WorkflowService workflowService, RunStore runStore, ReportStore reportStore) {
        this.workflowService = workflowService;
        this.runStore = runStore;
        this.reportStore = reportStore;
    }

    @PostMapping("/api/plans/{planId}/runs")
    public TestRun run(@PathVariable String planId) {
        return workflowService.runPlan(planId);
    }

    @GetMapping("/api/runs/{runId}")
    public TestRun getRun(@PathVariable String runId) {
        TestRun run = runStore.get(runId);
        if (run == null) {
            throw new IllegalArgumentException("Run not found: " + runId);
        }
        return run;
    }

    @PostMapping("/api/runs/{runId}/report")
    public RunReport createReport(@PathVariable String runId) {
        TestRun run = getRun(runId);
        return workflowService.buildReport(run);
    }

    @GetMapping(value = "/api/runs/{runId}/report.md", produces = MediaType.TEXT_PLAIN_VALUE)
    public Resource markdown(@PathVariable String runId) {
        return new FileSystemResource(reportStore.markdownPath(runId));
    }

    @GetMapping(value = "/api/runs/{runId}/report.html", produces = MediaType.TEXT_HTML_VALUE)
    public Resource html(@PathVariable String runId) {
        return new FileSystemResource(reportStore.htmlPath(runId));
    }
}
