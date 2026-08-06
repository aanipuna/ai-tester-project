package com.dialog.dtg.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EndpointSpec {

    private String endpointId;
    private String method;
    private String path;
    private String authType;
    private List<ParameterSpec> parameters = new ArrayList<>();
    private Map<String, Object> requestBodySchema;
    private int expectedSuccessStatus;
    private Map<String, Object> expectedResponseSchema;

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public List<ParameterSpec> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterSpec> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getRequestBodySchema() {
        return requestBodySchema;
    }

    public void setRequestBodySchema(Map<String, Object> requestBodySchema) {
        this.requestBodySchema = requestBodySchema;
    }

    public int getExpectedSuccessStatus() {
        return expectedSuccessStatus;
    }

    public void setExpectedSuccessStatus(int expectedSuccessStatus) {
        this.expectedSuccessStatus = expectedSuccessStatus;
    }

    public Map<String, Object> getExpectedResponseSchema() {
        return expectedResponseSchema;
    }

    public void setExpectedResponseSchema(Map<String, Object> expectedResponseSchema) {
        this.expectedResponseSchema = expectedResponseSchema;
    }
}
