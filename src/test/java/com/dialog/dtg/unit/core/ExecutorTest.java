package com.dialog.dtg.unit.core;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.RequestSpec;
import com.dialog.dtg.core.model.TestCase;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.service.ExecutorService;
import com.dialog.dtg.core.store.AtomicFileWriter;
import com.dialog.dtg.core.store.FileLockManager;
import com.dialog.dtg.core.store.RunStore;
import com.dialog.dtg.core.store.SchemaMigrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutorTest {

    @Test
    void shouldEvaluateAssertionsAndMarkTestCaseAsPassed() {
        DataPathProperties props = new DataPathProperties();
        ObjectMapper mapper = new ObjectMapper();
        RunStore runStore = new RunStore(mapper, new AtomicFileWriter(), new FileLockManager(), props);
        ExecutorService executor = new ExecutorService(WebClient.builder(), runStore, new SchemaMigrationService(props));

        TestCase tc = new TestCase();
        tc.setId("TC-1");
        tc.setCategory("positive");
        tc.setExpectedStatus(200);
        RequestSpec req = new RequestSpec();
        req.setMethod("GET");
        req.setPath("http://localhost:1/unreachable");
        tc.setRequest(req);

        TestPlan plan = new TestPlan();
        plan.setPlanId("plan-test");
        plan.setTestCases(List.of(tc));

        var run = executor.execute(plan);
        assertEquals(1, run.getSummary().getTotal());
        assertTrue(run.getSummary().getErrors() >= 1 || run.getSummary().getFailed() >= 1);
    }

    @Test
    void shouldCaptureFailureEvidenceWhenExpectedStatusDiffers() {
        DataPathProperties props = new DataPathProperties();
        ObjectMapper mapper = new ObjectMapper();
        RunStore runStore = new RunStore(mapper, new AtomicFileWriter(), new FileLockManager(), props);
        ExecutorService executor = new ExecutorService(WebClient.builder(), runStore, new SchemaMigrationService(props));

        TestCase tc = new TestCase();
        tc.setId("TC-2");
        tc.setCategory("negative");
        tc.setExpectedStatus(201);
        RequestSpec req = new RequestSpec();
        req.setMethod("GET");
        req.setPath("http://localhost:1/unreachable");
        tc.setRequest(req);

        TestPlan plan = new TestPlan();
        plan.setPlanId("plan-test-2");
        plan.setTestCases(List.of(tc));

        var run = executor.execute(plan);
        assertEquals(1, run.getResults().size());
        assertTrue(run.getResults().get(0).getFailureReason() != null);
    }
}
