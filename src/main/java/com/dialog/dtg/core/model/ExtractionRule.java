package com.dialog.dtg.core.model;

public class ExtractionRule {

    public enum Source { BODY, HEADER, STATUS }

    private String variableName;
    private Source source = Source.BODY;
    private String locator; // JSONPath for BODY, header name for HEADER, null for STATUS
    private boolean secret = false;

    public String getVariableName() { return variableName; }
    public void setVariableName(String variableName) { this.variableName = variableName; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public String getLocator() { return locator; }
    public void setLocator(String locator) { this.locator = locator; }

    public boolean isSecret() { return secret; }
    public void setSecret(boolean secret) { this.secret = secret; }
}
