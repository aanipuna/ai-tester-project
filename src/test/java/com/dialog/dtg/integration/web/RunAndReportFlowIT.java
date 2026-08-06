package com.dialog.dtg.integration.web;

import com.dialog.dtg.core.model.RunReport;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.ReportStore;
import com.dialog.dtg.core.store.RunStore;
import com.dialog.dtg.web.controller.RunController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunAndReportFlowIT {

    @Test
    void shouldExecuteRunAndBuildReport() {
        WorkflowService workflowService = Mockito.mock(WorkflowService.class);
        RunStore runStore = Mockito.mock(RunStore.class);
        ReportStore reportStore = Mockito.mock(ReportStore.class);

        TestRun run = new TestRun();
        run.setRunId("run-1");
        RunReport report = new RunReport();
        report.setRunId("run-1");

        Mockito.when(workflowService.runPlan("plan-1")).thenReturn(run);
        Mockito.when(runStore.get("run-1")).thenReturn(run);
        Mockito.when(workflowService.buildReport(run)).thenReturn(report);

        RunController controller = new RunController(workflowService, runStore, reportStore);
        assertEquals("run-1", controller.run("plan-1").getRunId());
        assertEquals("run-1", controller.createReport("run-1").getRunId());
    }
}
