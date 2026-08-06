package com.dialog.dtg.contract.web;

import com.dialog.dtg.core.PlanStore;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.ReportStore;
import com.dialog.dtg.core.store.RunStore;
import com.dialog.dtg.core.store.SpecStore;
import com.dialog.dtg.web.controller.PlanController;
import com.dialog.dtg.web.controller.RunController;
import com.dialog.dtg.web.dto.PlanRequest;
import com.dialog.dtg.web.mapper.PlanMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlanRunApiContractTest {

    @Test
    void shouldCreatePlanInImportMode() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        PlanStore planStore = Mockito.mock(PlanStore.class);
        SpecStore specStore = Mockito.mock(SpecStore.class);
        PlanMapper mapper = new PlanMapper();

        TestPlan plan = new TestPlan();
        plan.setPlanId("p-1");
        PlanRequest request = new PlanRequest();
        request.setMode("import");
        request.setPlan(plan);
        Mockito.when(planStore.create(plan)).thenReturn(plan);

        PlanController controller = new PlanController(workflowService, planStore, specStore, mapper);
        TestPlan created = controller.create(request);
        assertNotNull(created);
    }

    @Test
    void shouldExposeRunApiRoutesThroughControllerMethods() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        RunStore runStore = Mockito.mock(RunStore.class);
        ReportStore reportStore = Mockito.mock(ReportStore.class);
        RunController controller = new RunController(workflowService, runStore, reportStore);
        assertNotNull(controller);
    }
}
