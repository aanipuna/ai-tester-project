package com.dialog.dtg.integration.cli;

import com.dialog.dtg.cli.GenerateCommand;
import com.dialog.dtg.core.model.NormalizedSpec;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerateCommandIT {

    @Test
    void shouldGeneratePlanFromSpecAndPersistArtifacts() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        NormalizedSpec spec = new NormalizedSpec();
        spec.setSpecId("spec-123");
        TestPlan plan = new TestPlan();
        plan.setPlanId("plan-123");
        Mockito.when(workflowService.ingestSpec(Mockito.eq("openapi"), Mockito.anyString())).thenReturn(spec);
        Mockito.when(workflowService.generatePlanFromSpec(Mockito.eq(spec), Mockito.isNull())).thenReturn(plan);

        GenerateCommand command = new GenerateCommand(workflowService);
        int code = command.run(new String[]{"generate", "--spec", "sample.yaml"});
        assertEquals(0, code);
    }

    @Test
    void shouldReturnValidationErrorForMissingSpecInput() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        GenerateCommand command = new GenerateCommand(workflowService);
        int code = command.run(new String[]{"generate"});
        assertEquals(2, code);
    }
}
