package com.dialog.dtg.core.service;

import com.dialog.dtg.core.model.*;
import com.dialog.dtg.core.store.AuthConfigStore;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WorkflowExecutorService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutorService.class);
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");
    private static final int MAX_RESPONSE_SNAPSHOT = 2000;

    private final WebClient webClient;
    private final AuthConfigStore authConfigStore;
    private final ConditionEvaluator conditionEvaluator;

    public WorkflowExecutorService(WebClient.Builder webClientBuilder, AuthConfigStore authConfigStore,
                                   ConditionEvaluator conditionEvaluator) {
        this.webClient = webClientBuilder.build();
        this.authConfigStore = authConfigStore;
        this.conditionEvaluator = conditionEvaluator;
    }

    public WorkflowRun execute(Workflow workflow) {
        log.info("Executing workflow workflowId={} steps={}", workflow.getWorkflowId(), workflow.getSteps().size());
        WorkflowRun run = new WorkflowRun();
        run.setWorkflowRunId(Ids.nextWorkflowRunId());
        run.setWorkflowId(workflow.getWorkflowId());
        run.setWorkflowName(workflow.getName());
        run.setStartedAt(Instant.now());

        // Build variable context: workflow-level variables first
        Map<String, String> context = new LinkedHashMap<>(
            workflow.getVariables() != null ? workflow.getVariables() : Map.of());

        AuthConfig auth = authConfigStore.load();
        String baseUrl = workflow.getBaseUrl() != null ? workflow.getBaseUrl() : "";

        List<StepResult> results = new ArrayList<>();
        boolean halted = false;

        int stepIndex = 0;
        for (WorkflowStep step : workflow.getSteps()) {
            context.put("__stepIdx__" + step.getStepId(), String.valueOf(stepIndex + 1));
            StepResult result = new StepResult();
            result.setStepId(step.getStepId());
            result.setStepName(step.getName());

            if (halted) {
                result.setStatus("skipped");
                result.setFailureReason("Previous step halted workflow");
                results.add(result);
                continue;
            }

            // Evaluate conditions against already-recorded step results
            List<ConditionResult> condResults = evaluateConditions(step, results, context);
            result.setConditionResults(condResults);
            boolean conditionFailed = condResults.stream().anyMatch(c -> !c.isPassed());

            if (conditionFailed) {
                result.setStatus("skipped");
                result.setFailureReason("Condition not met: " +
                    condResults.stream().filter(c -> !c.isPassed()).findFirst()
                        .map(c -> c.getOperator() + " " + c.getActualValue() + " vs " + c.getExpectedValue())
                        .orElse("see condition results"));
                results.add(result);
                if (step.isHaltOnConditionFailure()) {
                    halted = true;
                }
                continue;
            }

            // Build and send request
            executeStep(step, baseUrl, context, auth, result);
            results.add(result);

            // Extract values into context
            extractValues(step, result, context);
            stepIndex++;
        }

        run.setFinishedAt(Instant.now());
        run.setStepResults(results);
        run.setStatus(computeRunStatus(results, halted));
        log.info("Workflow run completed workflowRunId={} status={}", run.getWorkflowRunId(), run.getStatus());
        return run;
    }

    private void executeStep(WorkflowStep step, String baseUrl, Map<String, String> context,
                             AuthConfig auth, StepResult result) {
        long start = System.currentTimeMillis();
        try {
            String method = step.getMethod();
            String resolvedPath = resolve(step.getPath(), context);
            String url = baseUrl + resolvedPath;

            // Build headers
            Map<String, String> resolvedHeaders = new LinkedHashMap<>();
            applyAuth(resolvedHeaders, auth);
            if (auth.getGlobalHeaders() != null) auth.getGlobalHeaders().forEach(resolvedHeaders::put);
            if (step.getHeaders() != null) step.getHeaders().forEach((k, v) -> resolvedHeaders.put(k, resolve(v, context)));

            // Process injections that target HEADER
            applyInjections(step, context, resolvedHeaders, null, null);

            String resolvedBody = step.getBody() != null ? resolve(step.getBody(), context) : null;
            // Apply BODY_FIELD injections
            resolvedBody = applyBodyInjections(step, context, resolvedBody);

            // Record snapshot
            RequestSnapshot snapshot = new RequestSnapshot();
            snapshot.setMethod(method);
            snapshot.setUrl(url);
            snapshot.setHeaders(resolvedHeaders);
            snapshot.setBody(resolvedBody);
            result.setRequestSent(snapshot);

            boolean hasBody = resolvedBody != null && !resolvedBody.isBlank()
                && !method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("HEAD") && !method.equalsIgnoreCase("DELETE");

            // Execute HTTP request
            final Map<String, String> finalHeaders = resolvedHeaders;
            final String finalBody = resolvedBody;
            WebClient.RequestHeadersSpec<?> reqSpec;
            if (hasBody) {
                reqSpec = webClient.method(HttpMethod.valueOf(method))
                    .uri(url).contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> finalHeaders.forEach(h::set))
                    .bodyValue(finalBody);
            } else {
                reqSpec = webClient.method(HttpMethod.valueOf(method))
                    .uri(url).headers(h -> finalHeaders.forEach(h::set));
            }

            int[] statusHolder = {0};
            String responseBody = reqSpec.exchangeToMono(r -> {
                statusHolder[0] = r.statusCode().value();
                return r.bodyToMono(String.class).defaultIfEmpty("");
            }).block();

            result.setHttpStatus(statusHolder[0]);
            result.setResponseTimeMs(System.currentTimeMillis() - start);
            if (responseBody != null && !responseBody.isBlank()) {
                result.setResponseBody(responseBody.length() > MAX_RESPONSE_SNAPSHOT
                    ? responseBody.substring(0, MAX_RESPONSE_SNAPSHOT) + "…[truncated]" : responseBody);
            }
            result.setStatus("pass"); // will be overridden if extraction fails

        } catch (Exception ex) {
            result.setStatus("error");
            result.setFailureReason(ex.getMessage());
            result.setResponseTimeMs(System.currentTimeMillis() - start);
        }
    }

    /** Resolve {{varName}} placeholders from context. */
    String resolve(String template, Map<String, String> context) {
        if (template == null) return null;
        Matcher m = VAR_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String key = m.group(1).trim();
            String val = context.getOrDefault(key, m.group(0)); // leave unresolved if missing
            m.appendReplacement(sb, Matcher.quoteReplacement(val));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private void extractValues(WorkflowStep step, StepResult result, Map<String, String> context) {
        if (step.getExtractions() == null) return;
        int idx = 0;
        // Step key prefix is the step's position index; use step index from workflow context (caller sets)
        String stepPrefix = "step" + findStepIndex(step, context) + ".";
        for (ExtractionRule rule : step.getExtractions()) {
            try {
                String value = switch (rule.getSource()) {
                    case STATUS -> result.getHttpStatus() != null ? String.valueOf(result.getHttpStatus()) : null;
                    case HEADER -> result.getRequestSent() != null
                        ? result.getRequestSent().getHeaders().get(rule.getLocator()) : null;
                    case BODY -> {
                        if (result.getResponseBody() == null) yield null;
                        try { yield String.valueOf(JsonPath.read(result.getResponseBody(), rule.getLocator())); }
                        catch (Exception e) { yield null; }
                    }
                };
                String contextKey = stepPrefix + rule.getVariableName();
                context.put(contextKey, value != null ? value : "");
                result.getExtractedValues().put(contextKey, rule.isSecret() ? "****" : (value != null ? value : ""));
            } catch (Exception ex) {
                log.warn("Extraction failed for variable={}: {}", rule.getVariableName(), ex.getMessage());
            }
            idx++;
        }
    }

    private List<ConditionResult> evaluateConditions(WorkflowStep step, List<StepResult> priorResults,
                                                      Map<String, String> context) {
        List<ConditionResult> out = new ArrayList<>();
        if (step.getConditions() == null) return out;
        int idx = 0;
        for (Condition cond : step.getConditions()) {
            StepResult priorStep = priorResults.stream()
                .filter(r -> r.getStepId().equals(cond.getSourceStepId())).findFirst().orElse(null);
            String actual = null;
            if (priorStep != null) {
                actual = switch (cond.getSource()) {
                    case STATUS -> priorStep.getHttpStatus() != null ? String.valueOf(priorStep.getHttpStatus()) : null;
                    case HEADER -> priorStep.getRequestSent() != null
                        ? priorStep.getRequestSent().getHeaders().get(cond.getLocator()) : null;
                    case BODY -> {
                        if (priorStep.getResponseBody() == null) yield null;
                        try { yield String.valueOf(JsonPath.read(priorStep.getResponseBody(), cond.getLocator())); }
                        catch (Exception e) { yield null; }
                    }
                };
            }
            boolean passed = conditionEvaluator.evaluate(cond.getOperator(), actual, cond.getExpectedValue());
            ConditionResult cr = new ConditionResult();
            cr.setConditionIndex(idx++);
            cr.setOperator(cond.getOperator().name());
            cr.setActualValue(actual);
            cr.setExpectedValue(cond.getExpectedValue());
            cr.setPassed(passed);
            out.add(cr);
        }
        return out;
    }

    private void applyInjections(WorkflowStep step, Map<String, String> context,
                                  Map<String, String> headers, Object bodyRef, Map<String, String> queryParams) {
        if (step.getInjections() == null) return;
        for (InjectionRule rule : step.getInjections()) {
            String resolved = resolve(rule.getVariableRef(), context);
            if (rule.getTarget() == InjectionRule.Target.HEADER && headers != null) {
                headers.put(rule.getTargetKey(), resolved);
            } else if (rule.getTarget() == InjectionRule.Target.QUERY_PARAM && queryParams != null) {
                queryParams.put(rule.getTargetKey(), resolved);
            }
            // BODY_FIELD and PATH are handled separately
        }
    }

    private String applyBodyInjections(WorkflowStep step, Map<String, String> context, String body) {
        if (step.getInjections() == null || body == null) return body;
        String result = body;
        for (InjectionRule rule : step.getInjections()) {
            if (rule.getTarget() == InjectionRule.Target.BODY_FIELD) {
                String resolved = resolve(rule.getVariableRef(), context);
                // Simple key replacement: "targetKey": "<placeholder>" → "targetKey": "resolved"
                result = result.replace("\"" + rule.getTargetKey() + "\": null",
                    "\"" + rule.getTargetKey() + "\": \"" + resolved + "\"");
                result = result.replace("\"" + rule.getTargetKey() + "\":null",
                    "\"" + rule.getTargetKey() + "\":\"" + resolved + "\"");
            }
        }
        return result;
    }

    private void applyAuth(Map<String, String> headers, AuthConfig auth) {
        if (auth == null || auth.getAuthType() == null) return;
        switch (auth.getAuthType()) {
            case bearer -> {
                if (auth.getToken() != null && !auth.getToken().isBlank())
                    headers.put("Authorization", "Bearer " + auth.getToken());
            }
            case basic -> {
                if (auth.getUsername() != null && !auth.getUsername().isBlank()) {
                    String creds = auth.getUsername() + ":" + (auth.getPassword() != null ? auth.getPassword() : "");
                    String encoded = java.util.Base64.getEncoder().encodeToString(
                        creds.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    headers.put("Authorization", "Basic " + encoded);
                }
            }
            case api_key -> {
                String headerName = auth.getApiKeyHeader() != null && !auth.getApiKeyHeader().isBlank()
                    ? auth.getApiKeyHeader() : "X-Api-Key";
                if (auth.getToken() != null && !auth.getToken().isBlank())
                    headers.put(headerName, auth.getToken());
            }
            default -> {}
        }
    }

    private String computeRunStatus(List<StepResult> results, boolean halted) {
        long errors = results.stream().filter(r -> "error".equals(r.getStatus())).count();
        long failures = results.stream().filter(r -> "fail".equals(r.getStatus())).count();
        long skipped = results.stream().filter(r -> "skipped".equals(r.getStatus())).count();
        if (errors > 0) return "error";
        if (failures > 0 || halted) return "failed";
        if (skipped > 0) return "partial";
        return "passed";
    }

    /** Tracks step index by counting prior results — used for step-scoped variable naming. */
    private int findStepIndex(WorkflowStep step, Map<String, String> context) {
        // We embed the step number in context under a hidden key during execution
        String key = "__stepIdx__" + step.getStepId();
        return Integer.parseInt(context.getOrDefault(key, "0"));
    }
}
