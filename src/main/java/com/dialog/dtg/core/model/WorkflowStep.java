package com.dialog.dtg.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkflowStep {

    private String stepId;
    private String name;
    private String method = "GET";
    private String path;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String body;
    private Map<String, String> queryParams = new LinkedHashMap<>();
    private int timeoutMs = 10000;
    private List<ExtractionRule> extractions = new ArrayList<>();
    private List<InjectionRule> injections = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();
    private boolean haltOnConditionFailure = false;

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers != null ? headers : new LinkedHashMap<>(); }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Map<String, String> getQueryParams() { return queryParams; }
    public void setQueryParams(Map<String, String> queryParams) { this.queryParams = queryParams != null ? queryParams : new LinkedHashMap<>(); }

    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }

    public List<ExtractionRule> getExtractions() { return extractions; }
    public void setExtractions(List<ExtractionRule> extractions) { this.extractions = extractions != null ? extractions : new ArrayList<>(); }

    public List<InjectionRule> getInjections() { return injections; }
    public void setInjections(List<InjectionRule> injections) { this.injections = injections != null ? injections : new ArrayList<>(); }

    public List<Condition> getConditions() { return conditions; }
    public void setConditions(List<Condition> conditions) { this.conditions = conditions != null ? conditions : new ArrayList<>(); }

    public boolean isHaltOnConditionFailure() { return haltOnConditionFailure; }
    public void setHaltOnConditionFailure(boolean haltOnConditionFailure) { this.haltOnConditionFailure = haltOnConditionFailure; }
}
