package com.dialog.dtg.integration.persistence;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.store.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestartReloadIT {

    @Test
    void shouldPersistAndReloadPlanFromDisk() {
        DataPathProperties props = new DataPathProperties();
        ObjectMapper mapper = new ObjectMapper();
        PlanJsonStore store = new PlanJsonStore(mapper, new AtomicFileWriter(), new FileLockManager(), props);

        TestPlan plan = new TestPlan();
        plan.setPlanId("reload-plan");
        store.save(plan);

        TestPlan reloaded = store.get("reload-plan");
        assertEquals("reload-plan", reloaded.getPlanId());
    }
}
