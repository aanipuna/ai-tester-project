package com.dialog.dtg.core.service;

import com.dialog.dtg.core.ScenarioGenerator;
import com.dialog.dtg.core.model.EndpointSpec;
import com.dialog.dtg.core.model.RequestSpec;
import com.dialog.dtg.core.model.TestCase;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.store.TemplateConfigStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScenarioGeneratorService implements ScenarioGenerator {

    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;
    private final TemplateConfigStore templateConfigStore;

    public ScenarioGeneratorService(ObjectMapper objectMapper, ObjectProvider<ChatClient.Builder> chatClientBuilder,
                                    TemplateConfigStore templateConfigStore) {
        this.objectMapper = objectMapper;
        this.templateConfigStore = templateConfigStore;
        ChatClient.Builder builder = chatClientBuilder.getIfAvailable();
        this.chatClient = builder == null ? null : builder.build();
    }

    @Override
    public TestPlan generatePlan(EndpointSpec endpointSpec) {
        String prompt = buildPrompt(endpointSpec);
        TestPlan parsed = tryGenerateFromModel(prompt);
        if (parsed != null) {
            if (parsed.getPlanId() == null || parsed.getPlanId().isBlank()) {
                parsed.setPlanId(Ids.nextPlanId());
            }
            parsed.setCreatedAt(Instant.now());
            parsed.setUpdatedAt(Instant.now());
            parsed.setStatus("active");
            return parsed;
        }
        return buildFallbackPlan(endpointSpec);
    }

    private TestPlan tryGenerateFromModel(String prompt) {
        if (chatClient == null) {
            return null;
        }
        try {
            String raw = chatClient.prompt(prompt).call().content();
            return objectMapper.readValue(raw, TestPlan.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private TestPlan buildFallbackPlan(EndpointSpec endpointSpec) {
        TestPlan plan = new TestPlan();
        plan.setPlanId(Ids.nextPlanId());
        plan.setSourceEndpoint(endpointSpec.getMethod() + " " + endpointSpec.getPath());
        plan.setCreatedBy("generated");
        plan.setCreatedAt(Instant.now());
        plan.setUpdatedAt(Instant.now());
        plan.setStatus("active");

        List<TestCase> cases = new ArrayList<>();
        cases.add(makeCase("TC-001", "positive", endpointSpec, endpointSpec.getExpectedSuccessStatus(), "Valid request should succeed"));
        cases.add(makeCase("TC-002", "negative", endpointSpec, 400, "Missing required field should fail"));
        cases.add(makeCase("TC-003", "boundary", endpointSpec, 400, "Boundary value behavior should be enforced"));
        if (!"none".equalsIgnoreCase(endpointSpec.getAuthType())) {
            cases.add(makeCase("TC-004", "auth", endpointSpec, 401, "Missing token should be unauthorized"));
        }
        cases.add(makeCase("TC-005", "idempotency", endpointSpec, endpointSpec.getExpectedSuccessStatus(), "Repeat call should remain consistent"));
        plan.setTestCases(cases);
        return plan;
    }

    private TestCase makeCase(String id, String category, EndpointSpec endpointSpec, int expectedStatus, String behavior) {
        TestCase tc = new TestCase();
        tc.setId(id);
        tc.setCategory(category);
        tc.setDescription(behavior);
        tc.setExpectedStatus(expectedStatus);
        tc.setExpectedBehavior(behavior);

        RequestSpec request = new RequestSpec();
        request.setMethod(endpointSpec.getMethod());
        request.setPath(endpointSpec.getPath());
        tc.setRequest(request);
        return tc;
    }

    private String buildPrompt(EndpointSpec endpointSpec) {
        StringBuilder params = new StringBuilder();
        endpointSpec.getParameters().forEach(p -> params.append("- ")
            .append(p.getName()).append(", ")
            .append(p.getDataType()).append(", ")
            .append(p.isRequired()).append(", ")
            .append(p.getLocation()).append(", ")
            .append(p.getConstraints()).append("\n"));

        String template = templateConfigStore.load().getPlanGenerationTemplate();
        return template
            .replace("{{method}}", endpointSpec.getMethod())
            .replace("{{path}}", endpointSpec.getPath())
            .replace("{{authType}}", endpointSpec.getAuthType() != null ? endpointSpec.getAuthType() : "none")
            .replace("{{parameters}}", params.toString())
            .replace("{{expectedStatus}}", String.valueOf(endpointSpec.getExpectedSuccessStatus()))
            .replace("{{expectedSchema}}", endpointSpec.getExpectedResponseSchema() != null ? endpointSpec.getExpectedResponseSchema().toString() : "");
    }
}
