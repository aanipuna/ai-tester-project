package com.dialog.dtg.unit.core;

import com.dialog.dtg.core.model.EndpointSpec;
import com.dialog.dtg.core.service.ScenarioGeneratorService;
import com.dialog.dtg.core.store.TemplateConfigStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioGeneratorTest {

    @Test
    void shouldGenerateFallbackPlanWhenNoLlmClientAvailable() {
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatClient.Builder> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(provider.getIfAvailable()).thenReturn(null);

        TemplateConfigStore mockTemplates = Mockito.mock(TemplateConfigStore.class);
        Mockito.when(mockTemplates.load()).thenReturn(new com.dialog.dtg.core.model.PromptTemplateConfig());
        ScenarioGeneratorService service = new ScenarioGeneratorService(
                new ObjectMapper(), provider, mockTemplates);
        EndpointSpec endpoint = new EndpointSpec();
        endpoint.setMethod("GET");
        endpoint.setPath("http://localhost:1");
        endpoint.setAuthType("none");
        endpoint.setExpectedSuccessStatus(200);
        endpoint.setParameters(new ArrayList<>());

        var plan = service.generatePlan(endpoint);
        assertNotNull(plan.getPlanId());
        assertTrue(plan.getTestCases().size() >= 3);
    }
}
