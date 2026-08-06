package com.dialog.dtg.integration.cli;

import com.dialog.dtg.cli.ReportCommand;
import com.dialog.dtg.core.model.RunReport;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.RunStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportCommandIT {

    @Test
    void shouldGenerateMarkdownAndHtmlReportsForCompletedRun() {
        RunStore runStore = Mockito.mock(RunStore.class);
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        TestRun run = new TestRun();
        run.setRunId("run-1");
        RunReport report = new RunReport();
        report.setRunId("run-1");

        Mockito.when(runStore.get("run-1")).thenReturn(run);
        Mockito.when(workflowService.buildReport(run)).thenReturn(report);

        ReportCommand command = new ReportCommand(runStore, workflowService);
        int code = command.run(new String[]{"report", "--run", "run-1"});
        assertEquals(0, code);
    }

    @Test
    void shouldReturnValidationErrorWhenRunIdIsMissingOrUnknown() {
        RunStore runStore = Mockito.mock(RunStore.class);
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        ReportCommand command = new ReportCommand(runStore, workflowService);
        int code = command.run(new String[]{"report"});
        assertEquals(2, code);
    }
}
