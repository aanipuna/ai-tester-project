package com.dialog.dtg.core.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class RequestSnapshot {

    private String method;
    private String url;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String body;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers != null ? headers : new LinkedHashMap<>(); }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
