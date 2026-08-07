package com.dialog.dtg.core.model;

public class AuthConfig {

    public enum AuthType { none, bearer, basic, api_key }

    private AuthType authType = AuthType.none;
    private String token;          // Bearer token or API key value
    private String username;       // Basic auth username
    private String password;       // Basic auth password
    private String apiKeyHeader;   // Header name for API key (e.g. X-Api-Key)

    public AuthType getAuthType() { return authType; }
    public void setAuthType(AuthType authType) { this.authType = authType; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getApiKeyHeader() { return apiKeyHeader; }
    public void setApiKeyHeader(String apiKeyHeader) { this.apiKeyHeader = apiKeyHeader; }

    public java.util.Map<String, String> getGlobalHeaders() { return globalHeaders; }
    public void setGlobalHeaders(java.util.Map<String, String> globalHeaders) { this.globalHeaders = globalHeaders != null ? globalHeaders : new java.util.LinkedHashMap<>(); }

    private java.util.Map<String, String> globalHeaders = new java.util.LinkedHashMap<>();
}
