package com.dialog.dtg.core.service;

import com.dialog.dtg.core.ReportBuilder;
import com.dialog.dtg.core.model.CaseResult;
import com.dialog.dtg.core.model.ReportMetrics;
import com.dialog.dtg.core.model.RunReport;
import com.dialog.dtg.core.model.TestRun;
import com.dialog.dtg.core.store.ReportStore;
import com.dialog.dtg.core.store.TemplateConfigStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportBuilderService implements ReportBuilder {

    private final ChatClient chatClient;
    private final ReportStore reportStore;
    private final TemplateConfigStore templateConfigStore;

    public ReportBuilderService(ObjectProvider<ChatClient.Builder> chatClientBuilder, ReportStore reportStore,
                                TemplateConfigStore templateConfigStore) {
        ChatClient.Builder builder = chatClientBuilder.getIfAvailable();
        this.chatClient = builder == null ? null : builder.build();
        this.reportStore = reportStore;
        this.templateConfigStore = templateConfigStore;
    }

    @Override
    public RunReport build(TestRun run) {
        RunReport report = new RunReport();
        report.setRunId(run.getRunId());
        report.setGeneratedAt(Instant.now());
        report.setMetrics(calculateMetrics(run));
        report.setResults(run.getResults());
        report.setNarrativeSummary(buildNarrative(report));
        reportStore.saveReport(run.getRunId(), report);
        return report;
    }

    private ReportMetrics calculateMetrics(TestRun run) {
        ReportMetrics metrics = new ReportMetrics();
        metrics.setTotal(run.getResults().size());
        metrics.setPassed((int) run.getResults().stream().filter(r -> "pass".equals(r.getStatus())).count());
        metrics.setFailed((int) run.getResults().stream().filter(r -> "fail".equals(r.getStatus())).count());
        metrics.setErrors((int) run.getResults().stream().filter(r -> "error".equals(r.getStatus())).count());

        Map<String, Integer> byCategory = new HashMap<>();
        for (CaseResult result : run.getResults()) {
            byCategory.merge(result.getCategory(), 1, Integer::sum);
        }
        metrics.setByCategory(byCategory);

        metrics.setSlowEndpointCount((int) run.getResults().stream().filter(r -> r.getResponseTimeMs() != null && r.getResponseTimeMs() > 2000).count());
        metrics.setFlakyEndpointCount(0);
        return metrics;
    }

    private String buildNarrative(RunReport report) {
        if (chatClient == null) {
            return "Run completed with %d passed, %d failed, and %d errors."
                .formatted(report.getMetrics().getPassed(), report.getMetrics().getFailed(), report.getMetrics().getErrors());
        }
        // build compact per-case summary for the prompt
        StringBuilder caseLines = new StringBuilder();
        if (report.getResults() != null) {
            for (var r : report.getResults()) {
                caseLines.append("- ").append(r.getTestCaseId())
                    .append(" [").append(r.getCategory()).append("] ")
                    .append(r.getStatus().toUpperCase())
                    .append(" HTTP=").append(r.getHttpStatus() != null ? r.getHttpStatus() : "N/A")
                    .append(" URL=").append(r.getRequestUrl() != null ? r.getRequestUrl() : "?")
                    .append(r.getFailureReason() != null ? " REASON: " + r.getFailureReason() : "")
                    .append("\n");
            }
        }
        String template = templateConfigStore.load().getReportNarrativeTemplate();
        String prompt = template
            .replace("{{passed}}", String.valueOf(report.getMetrics().getPassed()))
            .replace("{{failed}}", String.valueOf(report.getMetrics().getFailed()))
            .replace("{{errors}}", String.valueOf(report.getMetrics().getErrors()))
            .replace("{{slow}}", String.valueOf(report.getMetrics().getSlowEndpointCount()))
            .replace("{{caseResults}}", caseLines.toString());
        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception ex) {
            return "Run completed. Narrative generation failed, but deterministic metrics are available.";
        }
    }
}
