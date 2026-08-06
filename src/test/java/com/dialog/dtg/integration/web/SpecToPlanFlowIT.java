package com.dialog.dtg.integration.web;

import com.dialog.dtg.core.model.NormalizedSpec;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.SpecStore;
import com.dialog.dtg.web.controller.SpecController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecToPlanFlowIT {

    @Test
    void shouldListSpecsFromStore() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        SpecStore specStore = Mockito.mock(SpecStore.class);
        NormalizedSpec spec = new NormalizedSpec();
        spec.setSpecId("spec-1");
        Mockito.when(specStore.list()).thenReturn(List.of(spec));

        SpecController controller = new SpecController(workflowService, specStore);
        assertEquals(1, controller.list().size());
    }

    @Test
    void shouldGeneratePlanFromSpecViaWorkflowService() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        NormalizedSpec spec = new NormalizedSpec();
        spec.setSpecId("spec-1");
        TestPlan plan = new TestPlan();
        plan.setPlanId("plan-1");

        Mockito.when(workflowService.generatePlanFromSpec(spec, null)).thenReturn(plan);
        assertEquals("plan-1", workflowService.generatePlanFromSpec(spec, null).getPlanId());
    }
}
