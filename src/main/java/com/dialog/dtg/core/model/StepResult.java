package com.dialog.dtg.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StepResult {

    private String stepId;
    private String stepName;
    private String status; // pass | fail | skipped | error
    private RequestSnapshot requestSent;
    private Integer httpStatus;
    private String responseBody;
    private Long responseTimeMs;
    private Map<String, String> extractedValues = new LinkedHashMap<>();
    private List<ConditionResult> conditionResults = new ArrayList<>();
    private String failureReason;

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public RequestSnapshot getRequestSent() { return requestSent; }
    public void setRequestSent(RequestSnapshot requestSent) { this.requestSent = requestSent; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public Map<String, String> getExtractedValues() { return extractedValues; }
    public void setExtractedValues(Map<String, String> extractedValues) { this.extractedValues = extractedValues != null ? extractedValues : new LinkedHashMap<>(); }

    public List<ConditionResult> getConditionResults() { return conditionResults; }
    public void setConditionResults(List<ConditionResult> conditionResults) { this.conditionResults = conditionResults != null ? conditionResults : new ArrayList<>(); }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
