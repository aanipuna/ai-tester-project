package com.dialog.dtg.core.service;

import com.dialog.dtg.core.Executor;
import com.dialog.dtg.core.PlanStore;
import com.dialog.dtg.core.ReportBuilder;
import com.dialog.dtg.core.ScenarioGenerator;
import com.dialog.dtg.core.SpecParser;
import com.dialog.dtg.core.model.EndpointSpec;
import com.dialog.dtg.core.model.NormalizedSpec;
import com.dialog.dtg.core.model.RunReport;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.store.SchemaMigrationService;
import com.dialog.dtg.core.store.SpecStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private final SpecParser specParser;
    private final SpecStore specStore;
    private final ScenarioGenerator scenarioGenerator;
    private final PlanStore planStore;
    private final Executor executor;
    private final ReportBuilder reportBuilder;
    private final SchemaMigrationService schemaMigrationService;

    public WorkflowService(SpecParser specParser,
                           SpecStore specStore,
                           ScenarioGenerator scenarioGenerator,
                           PlanStore planStore,
                           Executor executor,
                           ReportBuilder reportBuilder,
                           SchemaMigrationService schemaMigrationService) {
        this.specParser = specParser;
        this.specStore = specStore;
        this.scenarioGenerator = scenarioGenerator;
        this.planStore = planStore;
        this.executor = executor;
        this.reportBuilder = reportBuilder;
        this.schemaMigrationService = schemaMigrationService;
    }

    public NormalizedSpec ingestSpec(String mode, String path) {
        log.info("Ingesting spec mode={} path={}", mode, path);
        NormalizedSpec spec = switch (mode) {
            case "openapi" -> specParser.parseFromOpenApi(path);
            case "postman" -> specParser.parseFromPostman(path);
            case "manual" -> specParser.parseFromManualJson(path);
            default -> throw new IllegalArgumentException("Unsupported spec mode: " + mode);
        };
        schemaMigrationService.applyDefaults(spec);
        NormalizedSpec saved = specStore.save(spec);
        log.info("Saved normalized spec specId={}", saved.getSpecId());
        return saved;
    }

    public TestPlan generatePlanFromSpec(NormalizedSpec spec, String endpointId) {
        log.info("Generating plan from specId={} endpointId={}", spec.getSpecId(), endpointId);
        EndpointSpec endpoint = spec.getEndpoints().stream()
            .filter(e -> endpointId == null || endpointId.isBlank() || endpointId.equals(e.getEndpointId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No endpoint found for requested ID"));

        TestPlan plan = scenarioGenerator.generatePlan(endpoint);
        plan.setSourceSpecId(spec.getSpecId());
        plan.setBaseUrl(spec.getBaseUrl());
        if (plan.getSourceEndpoint() == null) {
            plan.setSourceEndpoint(endpoint.getMethod() + " " + endpoint.getPath());
        }
        if (plan.getCreatedBy() == null) {
            plan.setCreatedBy("generated");
        }
        TestPlan created = planStore.create(plan);
        log.info("Created plan planId={} sourceSpecId={}", created.getPlanId(), created.getSourceSpecId());
        return created;
    }

    public List<TestPlan> generateAllPlansFromSpec(NormalizedSpec spec) {
        log.info("Generating plans for all {} endpoints in specId={}", spec.getEndpoints().size(), spec.getSpecId());
        List<TestPlan> created = new java.util.ArrayList<>();
        for (EndpointSpec endpoint : spec.getEndpoints()) {
            try {
                TestPlan plan = scenarioGenerator.generatePlan(endpoint);
                plan.setSourceSpecId(spec.getSpecId());
                plan.setBaseUrl(spec.getBaseUrl());
                if (plan.getSourceEndpoint() == null) {
                    plan.setSourceEndpoint(endpoint.getMethod() + " " + endpoint.getPath());
                }
                plan.setCreatedBy("generated");
                created.add(planStore.create(plan));
            } catch (Exception ex) {
                log.warn("Skipping endpoint {} {}: {}", endpoint.getMethod(), endpoint.getPath(), ex.getMessage());
            }
        }
        log.info("Created {} plans for specId={}", created.size(), spec.getSpecId());
        return created;
    }

    public TestRun runPlan(String planId) {
        log.info("Executing plan planId={}", planId);
        TestPlan plan = planStore.get(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan not found: " + planId);
        }
        TestRun run = executor.execute(plan);
        log.info("Completed run runId={} planId={} passed={} failed={} errors={}",
            run.getRunId(), run.getPlanId(), run.getSummary().getPassed(), run.getSummary().getFailed(), run.getSummary().getErrors());
        return run;
    }

    public RunReport buildReport(TestRun run) {
        log.info("Building report for runId={}", run.getRunId());
        RunReport report = reportBuilder.build(run);
        log.info("Report generated runId={}", report.getRunId());
        return report;
    }
}
