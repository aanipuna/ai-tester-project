package com.dialog.dtg.web.controller;

import com.dialog.dtg.core.PlanStore;
import com.dialog.dtg.core.model.NormalizedSpec;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.SpecStore;
import com.dialog.dtg.web.dto.PlanPatchRequest;
import com.dialog.dtg.web.dto.PlanRequest;
import com.dialog.dtg.web.mapper.PlanMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final WorkflowService workflowService;
    private final PlanStore planStore;
    private final SpecStore specStore;
    private final PlanMapper planMapper;

    public PlanController(WorkflowService workflowService, PlanStore planStore, SpecStore specStore, PlanMapper planMapper) {
        this.workflowService = workflowService;
        this.planStore = planStore;
        this.specStore = specStore;
        this.planMapper = planMapper;
    }

    @PostMapping
    public TestPlan create(@RequestBody PlanRequest request) {
        if ("import".equalsIgnoreCase(request.getMode())) {
            if (request.getPlan() == null) {
                throw new IllegalArgumentException("Plan payload is required for import mode");
            }
            request.getPlan().setCreatedBy("imported");
            return planStore.create(request.getPlan());
        }

        if (!"generate".equalsIgnoreCase(request.getMode())) {
            throw new IllegalArgumentException("Unsupported mode: " + request.getMode());
        }

        NormalizedSpec spec = specStore.get(request.getSpecId());
        if (spec == null) {
            throw new IllegalArgumentException("Spec not found: " + request.getSpecId());
        }
        return workflowService.generatePlanFromSpec(spec, request.getEndpointId());
    }

    @GetMapping
    public List<TestPlan> list() {
        return planStore.list();
    }

    @GetMapping("/{planId}")
    public TestPlan get(@PathVariable String planId) {
        TestPlan plan = planStore.get(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan not found: " + planId);
        }
        return plan;
    }

    @PatchMapping("/{planId}")
    public TestPlan patch(@PathVariable String planId, @RequestBody PlanPatchRequest patchRequest) {
        TestPlan plan = get(planId);
        planStore.update(planMapper.applyPatch(plan, patchRequest));
        return planStore.get(planId);
    }

    @DeleteMapping("/{planId}")
    public void delete(@PathVariable String planId) {
        if (!planStore.archive(planId)) {
            throw new IllegalArgumentException("Plan not found: " + planId);
        }
    }
}
