package com.dialog.dtg.web.dto;

import com.dialog.dtg.core.model.TestCase;

import java.util.List;

public class PlanPatchRequest {

    private String status;
    private String baseUrl;
    private List<TestCase> testCases;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}
