package com.dialog.dtg.core.store;

import com.dialog.dtg.core.model.RunReport;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Repository
public class ReportStore {

    private final AtomicFileWriter atomicFileWriter;
    private final RunStore runStore;

    public ReportStore(AtomicFileWriter atomicFileWriter, RunStore runStore) {
        this.atomicFileWriter = atomicFileWriter;
        this.runStore = runStore;
    }

    public void saveReport(String runId, RunReport report) {
        String md = toMarkdown(report);
        String html = toHtml(report);

        Path mdPath = runStore.runDirectory(runId).resolve("report.md");
        Path htmlPath = runStore.runDirectory(runId).resolve("report.html");

        try {
            Files.createDirectories(runStore.runDirectory(runId));
            atomicFileWriter.writeAtomically(mdPath, md.getBytes(StandardCharsets.UTF_8));
            atomicFileWriter.writeAtomically(htmlPath, html.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist report files", ex);
        }
    }

    public Path markdownPath(String runId) {
        return runStore.runDirectory(runId).resolve("report.md");
    }

    public Path htmlPath(String runId) {
        return runStore.runDirectory(runId).resolve("report.html");
    }

    private String toMarkdown(RunReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("# API Test Report\n\n")
          .append("- Run ID: ").append(report.getRunId()).append("\n")
          .append("- Generated: ").append(report.getGeneratedAt()).append("\n")
          .append("- Total: ").append(report.getMetrics().getTotal()).append("\n")
          .append("- Passed: ").append(report.getMetrics().getPassed()).append("\n")
          .append("- Failed: ").append(report.getMetrics().getFailed()).append("\n")
          .append("- Errors: ").append(report.getMetrics().getErrors()).append("\n\n")
          .append("## Narrative\n").append(report.getNarrativeSummary()).append("\n\n")
          .append("## Test Case Results\n\n")
          .append("| Case | Category | Status | HTTP | Time (ms) | Request | Response |\n")
          .append("|------|----------|--------|------|-----------|---------|----------|\n");
        if (report.getResults() != null) {
            for (var r : report.getResults()) {
                sb.append("| ").append(safe(r.getTestCaseId()))
                  .append(" | ").append(safe(r.getCategory()))
                  .append(" | ").append(safe(r.getStatus()))
                  .append(" | ").append(r.getHttpStatus() != null ? r.getHttpStatus() : "-")
                  .append(" | ").append(r.getResponseTimeMs() != null ? r.getResponseTimeMs() : "-")
                  .append(" | `").append(safe(r.getRequestUrl())).append("`")
                  .append(" | ").append(truncate(r.getResponseSnapshot()))
                  .append(" |\n");
                if (r.getRequestBody() != null && !r.getRequestBody().isBlank()) {
                    sb.append("  - **Request Body**: `").append(truncate(r.getRequestBody())).append("`\n");
                }
                if (r.getFailureReason() != null) {
                    sb.append("  - **Failure**: ").append(r.getFailureReason()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    private String toHtml(RunReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html lang='en'><head><meta charset='UTF-8'/><title>API Test Report</title>")
          .append("<style>body{font-family:Segoe UI,sans-serif;max-width:1100px;margin:24px auto;color:#132023}")
          .append("h1,h2{color:#1f6a73}table{width:100%;border-collapse:collapse;margin-top:12px}")
          .append("th,td{border:1px solid #d3d8da;padding:8px;text-align:left;vertical-align:top}")
          .append("th{background:#d9ecef}.pass{color:#155724}.fail{color:#721c24}.error{color:#856404}")
          .append(".snippet{font-family:monospace;font-size:0.82rem;max-width:280px;word-break:break-all}")
          .append("pre{background:#f4f4f4;padding:8px;border-radius:6px;white-space:pre-wrap;font-size:0.82rem}")
          .append("</style></head><body>");
        sb.append("<h1>API Test Report</h1>");
        sb.append("<ul><li>Run ID: <strong>").append(report.getRunId()).append("</strong></li>")
          .append("<li>Generated: ").append(report.getGeneratedAt()).append("</li>")
          .append("<li>Total: ").append(report.getMetrics().getTotal()).append("</li>")
          .append("<li>Passed: <span class='pass'>").append(report.getMetrics().getPassed()).append("</span></li>")
          .append("<li>Failed: <span class='fail'>").append(report.getMetrics().getFailed()).append("</span></li>")
          .append("<li>Errors: <span class='error'>").append(report.getMetrics().getErrors()).append("</span></li>")
          .append("</ul>");
        sb.append("<h2>Narrative</h2><p>").append(esc(report.getNarrativeSummary())).append("</p>");
        sb.append("<h2>Test Case Results</h2><table>")
          .append("<thead><tr><th>Case</th><th>Category</th><th>Status</th><th>HTTP</th><th>Time&nbsp;(ms)</th>")
          .append("<th>Request</th><th>Request Body</th><th>Response</th><th>Failure</th></tr></thead><tbody>");
        if (report.getResults() != null) {
            for (var r : report.getResults()) {
                String cls = "pass".equals(r.getStatus()) ? "pass" : "fail".equals(r.getStatus()) ? "fail" : "error";
                sb.append("<tr>")
                  .append("<td>").append(esc(r.getTestCaseId())).append("</td>")
                  .append("<td>").append(esc(r.getCategory())).append("</td>")
                  .append("<td class='").append(cls).append("'><strong>").append(esc(r.getStatus())).append("</strong></td>")
                  .append("<td>").append(r.getHttpStatus() != null ? r.getHttpStatus() : "-").append("</td>")
                  .append("<td>").append(r.getResponseTimeMs() != null ? r.getResponseTimeMs() : "-").append("</td>")
                  .append("<td class='snippet'>").append(esc(r.getRequestUrl())).append("</td>")
                  .append("<td class='snippet'>").append(r.getRequestBody() != null ? "<pre>" + esc(r.getRequestBody()) + "</pre>" : "-").append("</td>")
                  .append("<td class='snippet'>").append(r.getResponseSnapshot() != null ? "<pre>" + esc(r.getResponseSnapshot().toString()) + "</pre>" : "-").append("</td>")
                  .append("<td class='fail'>").append(r.getFailureReason() != null ? esc(r.getFailureReason()) : "").append("</td>")
                  .append("</tr>");
            }
        }
        sb.append("</tbody></table></body></html>");
        return sb.toString();
    }

    private String safe(Object v) { return v == null ? "-" : v.toString().replace("|", "\\|"); }
    private String truncate(Object v) { if (v == null) return "-"; String s = v.toString(); return s.length() > 80 ? s.substring(0, 80) + "…" : s; }
    private String esc(Object v) { if (v == null) return ""; return v.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
}
