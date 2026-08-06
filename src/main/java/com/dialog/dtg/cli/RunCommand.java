package com.dialog.dtg.cli;

import com.dialog.dtg.core.Executor;
import com.dialog.dtg.core.PlanStore;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.service.WorkflowService;
import org.springframework.stereotype.Component;

@Component
public class RunCommand {

    private final WorkflowService workflowService;

    public RunCommand(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public int run(String[] args) {
        String planValue = valueOf(args, "--plan");
        if (planValue == null) {
            System.err.println(CliErrorFormatter.format("VALIDATION_ERROR", "Missing required option: --plan <plan-id>"));
            return 2;
        }
        try {
            TestRun run = workflowService.runPlan(stripExt(planValue));
            System.out.println("Run completed: runId=" + run.getRunId());
            System.out.println("Passed=" + run.getSummary().getPassed() + " Failed=" + run.getSummary().getFailed() + " Errors=" + run.getSummary().getErrors());
            if (run.getSummary().getFailed() > 0 || run.getSummary().getErrors() > 0) {
                return 1;
            }
            return 0;
        } catch (IllegalArgumentException ex) {
            System.err.println(CliErrorFormatter.format("VALIDATION_ERROR", ex.getMessage()));
            return 2;
        } catch (Exception ex) {
            System.err.println(CliErrorFormatter.format("EXECUTION_ERROR", ex.getMessage()));
            return 4;
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

    private String stripExt(String input) {
        return input.endsWith(".json") ? input.substring(0, input.length() - 5) : input;
    }
}
