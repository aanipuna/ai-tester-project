package com.dialog.dtg.core.model;

public class ConditionResult {

    private int conditionIndex;
    private String operator;
    private String actualValue;
    private String expectedValue;
    private boolean passed;

    public int getConditionIndex() { return conditionIndex; }
    public void setConditionIndex(int conditionIndex) { this.conditionIndex = conditionIndex; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getActualValue() { return actualValue; }
    public void setActualValue(String actualValue) { this.actualValue = actualValue; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
}
