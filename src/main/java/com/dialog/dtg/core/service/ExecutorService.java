package com.dialog.dtg.core.service;

import com.dialog.dtg.core.Executor;
import com.dialog.dtg.core.model.AssertionResult;
import com.dialog.dtg.core.model.CaseResult;
import com.dialog.dtg.core.model.RunSummary;
import com.dialog.dtg.core.model.TestCase;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.store.RunStore;
import com.dialog.dtg.core.store.SchemaMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExecutorService implements Executor {

    private static final Logger log = LoggerFactory.getLogger(ExecutorService.class);

    private final WebClient webClient;
    private final RunStore runStore;
    private final SchemaMigrationService schemaMigrationService;

    public ExecutorService(WebClient.Builder webClientBuilder, RunStore runStore, SchemaMigrationService schemaMigrationService) {
        this.webClient = webClientBuilder.build();
        this.runStore = runStore;
        this.schemaMigrationService = schemaMigrationService;
    }

    @Override
    public TestRun execute(TestPlan plan) {
        log.info("Starting execution for planId={} cases={}", plan.getPlanId(), plan.getTestCases().size());
        TestRun run = new TestRun();
        run.setRunId(Ids.nextRunId());
        run.setPlanId(plan.getPlanId());
        run.setTriggeredBy("cli");
        run.setStartedAt(Instant.now());

        String baseUrl = plan.getBaseUrl() != null ? plan.getBaseUrl() : "";
        List<CaseResult> results = new ArrayList<>();
        for (TestCase tc : plan.getTestCases()) {
            if (!tc.isEnabled()) {
                CaseResult skipped = new CaseResult();
                skipped.setTestCaseId(tc.getId());
                skipped.setCategory(tc.getCategory());
                skipped.setStatus("skipped");
                results.add(skipped);
                continue;
            }
            results.add(runCase(tc, baseUrl));
        }

        run.setFinishedAt(Instant.now());
        run.setResults(results);
        run.setSummary(summarize(results));
        schemaMigrationService.applyDefaults(run);
        runStore.save(run);
        log.info("Stored run results runId={} total={} passed={} failed={} errors={}",
            run.getRunId(), run.getSummary().getTotal(), run.getSummary().getPassed(), run.getSummary().getFailed(), run.getSummary().getErrors());
        return run;
    }

    private CaseResult runCase(TestCase tc, String baseUrl) {
        CaseResult result = new CaseResult();
        result.setTestCaseId(tc.getId());
        result.setCategory(tc.getCategory());

        long start = System.currentTimeMillis();
        try {
            String uri = baseUrl + tc.getRequest().getPath();
            String reqBody = tc.getRequest().getBody() == null ? "" : tc.getRequest().getBody().toString();
            String method = tc.getRequest().getMethod();
            result.setRequestUrl(method + " " + uri);
            result.setRequestBody(reqBody.isBlank() ? null : reqBody);

            boolean hasBody = !reqBody.isBlank() && !method.equalsIgnoreCase("GET")
                    && !method.equalsIgnoreCase("HEAD") && !method.equalsIgnoreCase("DELETE");

            var spec = webClient.method(HttpMethod.valueOf(method))
                .uri(uri)
                .headers(headers -> {
                    headers.setAll(tc.getRequest().getHeaders());
                    if (hasBody) headers.setContentType(MediaType.APPLICATION_JSON);
                });

            // Capture status code before body Mono to avoid null when body is empty
            int status = spec.exchangeToMono(r -> {
                    int code = r.statusCode().value();
                    return r.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> {
                                result.setResponseSnapshot(body.isBlank() ? null : body);
                                return code;
                            });
                })
                .block();

            long elapsed = System.currentTimeMillis() - start;
            result.setHttpStatus(status);
            result.setResponseTimeMs(elapsed);

            AssertionResult ar = new AssertionResult();
            ar.setAssertionType("status");
            ar.setExpected(tc.getExpectedStatus());
            ar.setActual(status);
            ar.setPassed(status == tc.getExpectedStatus());
            ar.setMessage(ar.isPassed() ? "Status matched expected value" : "Status mismatch");

            result.setAssertionResults(List.of(ar));
            if (ar.isPassed()) {
                result.setStatus("pass");
            } else {
                result.setStatus("fail");
                result.setFailureReason("Expected status %d but got %d".formatted(tc.getExpectedStatus(), status));
            }
        } catch (Exception ex) {
            result.setStatus("error");
            result.setFailureReason(ex.getMessage());
            result.setResponseTimeMs(System.currentTimeMillis() - start);
        }

        return result;
    }

    private RunSummary summarize(List<CaseResult> results) {
        RunSummary summary = new RunSummary();
        summary.setTotal(results.size());
        summary.setPassed((int) results.stream().filter(r -> "pass".equals(r.getStatus())).count());
        summary.setFailed((int) results.stream().filter(r -> "fail".equals(r.getStatus())).count());
        summary.setErrors((int) results.stream().filter(r -> "error".equals(r.getStatus())).count());
        summary.setSkipped((int) results.stream().filter(r -> "skipped".equals(r.getStatus())).count());
        return summary;
    }
}
