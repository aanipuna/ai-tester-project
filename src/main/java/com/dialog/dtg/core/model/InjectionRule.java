package com.dialog.dtg.core.model;

public class InjectionRule {

    public enum Target { HEADER, BODY_FIELD, QUERY_PARAM, PATH }

    private Target target = Target.HEADER;
    private String targetKey;      // header name / body field path / query param name / path param name
    private String variableRef;    // e.g. "{{step1.authToken}}" or "{{testMobile}}"

    public Target getTarget() { return target; }
    public void setTarget(Target target) { this.target = target; }

    public String getTargetKey() { return targetKey; }
    public void setTargetKey(String targetKey) { this.targetKey = targetKey; }

    public String getVariableRef() { return variableRef; }
    public void setVariableRef(String variableRef) { this.variableRef = variableRef; }
}
