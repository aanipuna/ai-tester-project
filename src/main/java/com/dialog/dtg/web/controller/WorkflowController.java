package com.dialog.dtg.web.controller;

import com.dialog.dtg.core.model.Workflow;
import com.dialog.dtg.core.model.WorkflowRun;
import com.dialog.dtg.core.service.Ids;
import com.dialog.dtg.core.service.WorkflowExecutorService;
import com.dialog.dtg.core.store.WorkflowJsonStore;
import com.dialog.dtg.core.store.WorkflowRunStore;
import com.dialog.dtg.web.dto.WorkflowListItem;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
public class WorkflowController {

    private final WorkflowJsonStore workflowStore;
    private final WorkflowRunStore workflowRunStore;
    private final WorkflowExecutorService executor;

    public WorkflowController(WorkflowJsonStore workflowStore, WorkflowRunStore workflowRunStore,
                               WorkflowExecutorService executor) {
        this.workflowStore = workflowStore;
        this.workflowRunStore = workflowRunStore;
        this.executor = executor;
    }

    @PostMapping("/api/workflows")
    @ResponseStatus(HttpStatus.CREATED)
    public Workflow create(@RequestBody Workflow workflow) {
        if (workflow.getName() == null || workflow.getName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workflow name is required");
        if (workflow.getSteps() == null || workflow.getSteps().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workflow must have at least one step");
        workflow.setWorkflowId(Ids.nextWorkflowId());
        workflow.setStatus("active");
        workflow.setCreatedAt(Instant.now());
        workflow.setUpdatedAt(Instant.now());
        // Assign step IDs if missing
        for (var step : workflow.getSteps()) {
            if (step.getStepId() == null || step.getStepId().isBlank())
                step.setStepId(Ids.nextStepId());
        }
        return workflowStore.save(workflow);
    }

    @GetMapping("/api/workflows")
    public List<WorkflowListItem> list() {
        return workflowStore.list().stream()
            .map(w -> new WorkflowListItem(
                w.getWorkflowId(), w.getName(), w.getDescription(),
                w.getSteps().size(), w.getStatus(),
                w.getUpdatedAt() != null ? w.getUpdatedAt().toString() : ""))
            .toList();
    }

    @GetMapping("/api/workflows/{workflowId}")
    public Workflow get(@PathVariable String workflowId) {
        Workflow w = workflowStore.get(workflowId);
        if (w == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found: " + workflowId);
        return w;
    }

    @PutMapping("/api/workflows/{workflowId}")
    public Workflow update(@PathVariable String workflowId, @RequestBody Workflow workflow) {
        if (workflowStore.get(workflowId) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found: " + workflowId);
        workflow.setWorkflowId(workflowId);
        workflow.setUpdatedAt(Instant.now());
        for (var step : workflow.getSteps()) {
            if (step.getStepId() == null || step.getStepId().isBlank())
                step.setStepId(Ids.nextStepId());
        }
        return workflowStore.save(workflow);
    }

    @DeleteMapping("/api/workflows/{workflowId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workflowId) {
        if (!workflowStore.delete(workflowId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found: " + workflowId);
    }

    @PostMapping("/api/workflows/{workflowId}/runs")
    public WorkflowRun run(@PathVariable String workflowId) {
        Workflow workflow = workflowStore.get(workflowId);
        if (workflow == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found: " + workflowId);
        WorkflowRun run = executor.execute(workflow);
        return workflowRunStore.save(run);
    }
    @GetMapping("/api/workflow-runs")
    public List<WorkflowRun> listRuns() {
        return workflowRunStore.list();
    }

    @GetMapping("/api/workflow-runs/{runId}")
    public WorkflowRun getRun(@PathVariable String runId) {
        WorkflowRun run = workflowRunStore.get(runId);
        if (run == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found: " + runId);
        return run;
    }
}
