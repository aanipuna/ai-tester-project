package com.dialog.dtg.core.model;

public class PromptTemplateConfig {

    private String planGenerationTemplate;
    private String reportNarrativeTemplate;

    // Default plan template — variables: {{method}} {{path}} {{authType}} {{parameters}} {{expectedStatus}} {{expectedSchema}}
    public static final String DEFAULT_PLAN_TEMPLATE =
        "You are an API test scenario generator. Given the API specification below, generate a comprehensive test plan.\n\n" +
        "## API Specification\n" +
        "Endpoint: {{method}} {{path}}\n" +
        "Auth type: {{authType}}\n" +
        "Parameters:\n{{parameters}}" +
        "Expected success response: {{expectedStatus}} — {{expectedSchema}}\n\n" +
        "## Instructions\n" +
        "Generate test cases covering ALL of these categories:\n" +
        "1. POSITIVE — valid requests using realistic sample data for every required and optional field combination\n" +
        "2. NEGATIVE — missing required fields (one omitted at a time), wrong data types, malformed values\n" +
        "3. BOUNDARY — min/max length strings, min/max numeric values, empty strings, zero, negative numbers where applicable\n" +
        "4. AUTH — missing token, expired/invalid token, insufficient scope/role (only if authType != none)\n" +
        "5. IDEMPOTENCY/DUPLICATE — repeated identical requests where relevant (e.g. POST creating a resource)\n\n" +
        "For each test case, infer a realistic expected HTTP status code based on standard REST conventions and the constraints given.\n\n" +
        "## Output format\n" +
        "Return ONLY valid JSON, no markdown fences, no commentary, matching exactly this schema:\n" +
        "{\n" +
        "  \"planId\": \"string\",\n" +
        "  \"sourceEndpoint\": \"string\",\n" +
        "  \"testCases\": [\n" +
        "    {\n" +
        "      \"id\": \"string (e.g. TC-001)\",\n" +
        "      \"category\": \"positive | negative | boundary | auth | idempotency\",\n" +
        "      \"description\": \"string\",\n" +
        "      \"request\": { \"method\": \"string\", \"path\": \"string\", \"headers\": {}, \"queryParams\": {}, \"body\": {} },\n" +
        "      \"expectedStatus\": number,\n" +
        "      \"expectedBehavior\": \"string\"\n" +
        "    }\n" +
        "  ]\n" +
        "}\n" +
        "Generate between 8 and 20 test cases depending on parameter complexity. Do not skip any required field for negative testing.\n";

    // Default report template — variables: {{passed}} {{failed}} {{errors}} {{slow}} {{caseResults}}
    public static final String DEFAULT_REPORT_TEMPLATE =
        "Summarize this API test run in 3-5 sentences. Focus on failures and errors.\n" +
        "Passed={{passed}} Failed={{failed}} Errors={{errors}} Slow={{slow}}\n\n" +
        "Case Results:\n{{caseResults}}";

    public String getPlanGenerationTemplate() {
        return planGenerationTemplate != null ? planGenerationTemplate : DEFAULT_PLAN_TEMPLATE;
    }

    public void setPlanGenerationTemplate(String planGenerationTemplate) {
        this.planGenerationTemplate = planGenerationTemplate;
    }

    public String getReportNarrativeTemplate() {
        return reportNarrativeTemplate != null ? reportNarrativeTemplate : DEFAULT_REPORT_TEMPLATE;
    }

    public void setReportNarrativeTemplate(String reportNarrativeTemplate) {
        this.reportNarrativeTemplate = reportNarrativeTemplate;
    }
}
