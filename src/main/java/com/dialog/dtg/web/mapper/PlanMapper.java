package com.dialog.dtg.web.mapper;

import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.web.dto.PlanPatchRequest;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {

    public TestPlan applyPatch(TestPlan plan, PlanPatchRequest patch) {
        if (patch.getStatus() != null && !patch.getStatus().isBlank()) {
            plan.setStatus(patch.getStatus());
        }
        if (patch.getBaseUrl() != null && !patch.getBaseUrl().isBlank()) {
            plan.setBaseUrl(patch.getBaseUrl());
        }
        if (patch.getTestCases() != null) {
            plan.setTestCases(patch.getTestCases());
        }
        return plan;
    }
}
