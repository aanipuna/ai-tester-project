package com.dialog.dtg.core.model;

public class Condition {

    public enum Source { STATUS, BODY, HEADER }

    public enum Operator { EQ, NE, CONTAINS, EXISTS, EMPTY, LT_STATUS }

    private String sourceStepId;
    private Source source = Source.STATUS;
    private String locator; // JSONPath or header name; null for STATUS
    private Operator operator = Operator.EQ;
    private String expectedValue;

    public String getSourceStepId() { return sourceStepId; }
    public void setSourceStepId(String sourceStepId) { this.sourceStepId = sourceStepId; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public String getLocator() { return locator; }
    public void setLocator(String locator) { this.locator = locator; }

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
}
