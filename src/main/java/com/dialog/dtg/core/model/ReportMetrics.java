package com.dialog.dtg.core.model;

import java.util.HashMap;
import java.util.Map;

public class ReportMetrics {

    private int total;
    private int passed;
    private int failed;
    private int errors;
    private Map<String, Integer> byCategory = new HashMap<>();
    private int slowEndpointCount;
    private int flakyEndpointCount;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(int passed) {
        this.passed = passed;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public Map<String, Integer> getByCategory() {
        return byCategory;
    }

    public void setByCategory(Map<String, Integer> byCategory) {
        this.byCategory = byCategory;
    }

    public int getSlowEndpointCount() {
        return slowEndpointCount;
    }

    public void setSlowEndpointCount(int slowEndpointCount) {
        this.slowEndpointCount = slowEndpointCount;
    }

    public int getFlakyEndpointCount() {
        return flakyEndpointCount;
    }

    public void setFlakyEndpointCount(int flakyEndpointCount) {
        this.flakyEndpointCount = flakyEndpointCount;
    }
}
