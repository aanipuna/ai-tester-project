package com.dialog.dtg.integration.cli;

import com.dialog.dtg.cli.RunCommand;
import com.dialog.dtg.core.model.RunSummary;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunCommandIT {

    @Test
    void shouldExitZeroWhenAllTestCasesPass() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        TestRun run = new TestRun();
        RunSummary summary = new RunSummary();
        summary.setPassed(2);
        summary.setFailed(0);
        summary.setErrors(0);
        run.setRunId("run-ok");
        run.setSummary(summary);

        Mockito.when(workflowService.runPlan("plan-1")).thenReturn(run);
        RunCommand command = new RunCommand(workflowService);
        int code = command.run(new String[]{"run", "--plan", "plan-1"});
        assertEquals(0, code);
    }

    @Test
    void shouldExitNonZeroWhenOneOrMoreTestCasesFail() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        TestRun run = new TestRun();
        RunSummary summary = new RunSummary();
        summary.setPassed(1);
        summary.setFailed(1);
        summary.setErrors(0);
        run.setRunId("run-fail");
        run.setSummary(summary);

        Mockito.when(workflowService.runPlan("plan-2")).thenReturn(run);
        RunCommand command = new RunCommand(workflowService);
        int code = command.run(new String[]{"run", "--plan", "plan-2"});
        assertEquals(1, code);
    }
}
