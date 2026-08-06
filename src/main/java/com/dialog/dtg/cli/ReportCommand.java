package com.dialog.dtg.cli;

import com.dialog.dtg.core.ReportBuilder;
import com.dialog.dtg.core.model.RunReport;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.RunStore;
import org.springframework.stereotype.Component;

@Component
public class ReportCommand {

    private final RunStore runStore;
    private final WorkflowService workflowService;

    public ReportCommand(RunStore runStore, WorkflowService workflowService) {
        this.runStore = runStore;
        this.workflowService = workflowService;
    }

    public int run(String[] args) {
        String runId = valueOf(args, "--run");
        if (runId == null) {
            System.err.println(CliErrorFormatter.format("VALIDATION_ERROR", "Missing required option: --run <run-id>"));
            return 2;
        }

        try {
            TestRun run = runStore.get(runId);
            if (run == null) {
                System.err.println(CliErrorFormatter.format("VALIDATION_ERROR", "Run not found: " + runId));
                return 2;
            }
            RunReport report = workflowService.buildReport(run);
            System.out.println("Report generated for runId=" + report.getRunId());
            return 0;
        } catch (Exception ex) {
            System.err.println(CliErrorFormatter.format("REPORT_ERROR", ex.getMessage()));
            return 3;
        }
    }

    private String valueOf(String[] args, String key) {
        for (int i = 0; i < args.length - 1; i++) {
            if (key.equals(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }
}
