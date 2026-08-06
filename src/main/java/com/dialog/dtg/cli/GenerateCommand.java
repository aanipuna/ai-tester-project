package com.dialog.dtg.cli;

import com.dialog.dtg.core.model.NormalizedSpec;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.PlanStore;
import com.dialog.dtg.core.ScenarioGenerator;
import com.dialog.dtg.core.SpecParser;
import com.dialog.dtg.core.service.WorkflowService;
import org.springframework.stereotype.Component;

@Component
public class GenerateCommand {

    private final WorkflowService workflowService;

    public GenerateCommand(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public int run(String[] args) {
        try {
            String specPath = valueOf(args, "--spec");
            String postmanPath = valueOf(args, "--postman");
            String manualPath = valueOf(args, "--manual");

            int selected = countNonNull(specPath, postmanPath, manualPath);
            if (selected != 1) {
                System.err.println(CliErrorFormatter.format("VALIDATION_ERROR", "Exactly one source must be provided: --spec, --postman, or --manual"));
                return 2;
            }

            String mode = specPath != null ? "openapi" : (postmanPath != null ? "postman" : "manual");
            String path = specPath != null ? specPath : (postmanPath != null ? postmanPath : manualPath);
            NormalizedSpec spec = workflowService.ingestSpec(mode, path);
            TestPlan plan = workflowService.generatePlanFromSpec(spec, null);
            System.out.println("Generated specId=" + spec.getSpecId() + " planId=" + plan.getPlanId());
            return 0;
        } catch (IllegalArgumentException ex) {
            System.err.println(CliErrorFormatter.format("VALIDATION_ERROR", ex.getMessage()));
            return 2;
        } catch (Exception ex) {
            System.err.println(CliErrorFormatter.format("GENERATION_ERROR", ex.getMessage()));
            return 3;
        }
    }

    private int countNonNull(String... values) {
        int count = 0;
        for (String value : values) {
            if (value != null) {
                count++;
            }
        }
        return count;
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
