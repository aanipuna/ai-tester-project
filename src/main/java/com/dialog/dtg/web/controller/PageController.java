package com.dialog.dtg.web.controller;

import com.dialog.dtg.core.PlanStore;
import com.dialog.dtg.core.model.TestCase;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.AuthConfigStore;
import com.dialog.dtg.core.store.ReportStore;
import com.dialog.dtg.core.store.RunStore;
import com.dialog.dtg.core.store.SpecStore;
import com.dialog.dtg.core.store.TemplateConfigStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Controller
public class PageController {

    private final SpecStore specStore;
    private final PlanStore planStore;
    private final RunStore runStore;
    private final ReportStore reportStore;
    private final WorkflowService workflowService;
    private final TemplateConfigStore templateConfigStore;
    private final AuthConfigStore authConfigStore;

    public PageController(SpecStore specStore, PlanStore planStore, RunStore runStore,
                          ReportStore reportStore, WorkflowService workflowService,
                          TemplateConfigStore templateConfigStore, AuthConfigStore authConfigStore) {
        this.specStore = specStore;
        this.planStore = planStore;
        this.runStore = runStore;
        this.reportStore = reportStore;
        this.workflowService = workflowService;
        this.templateConfigStore = templateConfigStore;
        this.authConfigStore = authConfigStore;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("specs", specStore.list());
        model.addAttribute("plans", planStore.list());
        return "specs";
    }

    @GetMapping("/specs")
    public String specs(Model model) {
        model.addAttribute("specs", specStore.list());
        return "specs";
    }

    @GetMapping("/plans")
    public String plans(Model model) {
        model.addAttribute("plans", planStore.list());
        model.addAttribute("specs", specStore.list());
        return "plans";
    }

    @GetMapping("/plans/{planId}")
    public String planDetail(@PathVariable String planId, Model model) {
        TestPlan plan = planStore.get(planId);
        if (plan == null) throw new IllegalArgumentException("Plan not found: " + planId);
        model.addAttribute("plan", plan);
        return "plan-detail";
    }

    @GetMapping("/runs")
    public String runs(Model model) {
        model.addAttribute("runs", runStore.list());
        model.addAttribute("plans", planStore.list());
        return "runs";
    }

    @GetMapping("/runs/{runId}")
    public String runDetail(@PathVariable String runId, Model model) {
        TestRun run = runStore.get(runId);
        if (run == null) throw new IllegalArgumentException("Run not found: " + runId);
        model.addAttribute("run", run);
        // attach report narrative if the markdown file exists
        var mdPath = reportStore.markdownPath(runId);
        if (Files.exists(mdPath)) {
            try {
                model.addAttribute("reportNarrative", Files.readString(mdPath, StandardCharsets.UTF_8));
            } catch (IOException ignored) {}
        }
        return "run-detail";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("templates", templateConfigStore.load());
        model.addAttribute("authConfig", authConfigStore.load());
        return "settings";
    }

    @PostMapping("/ui/specs/{specId}/generate")
    public String generatePlan(@PathVariable String specId) {
        var spec = specStore.get(specId);
        if (spec != null) workflowService.generatePlanFromSpec(spec, null);
        return "redirect:/plans";
    }

    @PostMapping("/ui/plans/{planId}/run")
    public String runPlan(@PathVariable String planId) {
        workflowService.runPlan(planId);
        return "redirect:/runs";
    }

    @PostMapping("/ui/plans/{planId}/cases/{caseId}/toggle")
    public String toggleCase(@PathVariable String planId, @PathVariable String caseId) {
        TestPlan plan = planStore.get(planId);
        if (plan != null) {
            for (TestCase tc : plan.getTestCases()) {
                if (caseId.equals(tc.getId())) {
                    tc.setEnabled(!tc.isEnabled());
                    break;
                }
            }
            planStore.update(plan);
        }
        return "redirect:/plans/" + planId;
    }

    @PostMapping("/ui/runs/{runId}/report")
    public String generateReport(@PathVariable String runId) {
        var run = runStore.get(runId);
        if (run != null) workflowService.buildReport(run);
        return "redirect:/runs/" + runId;
    }

    @PostMapping("/ui/specs/{specId}/delete")
    public String deleteSpec(@PathVariable String specId) {
        specStore.delete(specId);
        return "redirect:/specs";
    }

    @PostMapping("/ui/plans/{planId}/delete")
    public String deletePlan(@PathVariable String planId) {
        planStore.delete(planId);
        return "redirect:/plans";
    }

    @PostMapping("/ui/runs/{runId}/delete")
    public String deleteRun(@PathVariable String runId) {
        runStore.delete(runId);
        return "redirect:/runs";
    }
}
