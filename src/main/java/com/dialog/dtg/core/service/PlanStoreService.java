package com.dialog.dtg.core.service;

import com.dialog.dtg.core.PlanStore;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.store.PlanJsonStore;
import com.dialog.dtg.core.store.SchemaMigrationService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PlanStoreService implements PlanStore {

    private final PlanJsonStore planJsonStore;
    private final SchemaMigrationService schemaMigrationService;

    public PlanStoreService(PlanJsonStore planJsonStore, SchemaMigrationService schemaMigrationService) {
        this.planJsonStore = planJsonStore;
        this.schemaMigrationService = schemaMigrationService;
    }

    @Override
    public TestPlan create(TestPlan plan) {
        if (plan.getPlanId() == null || plan.getPlanId().isBlank()) {
            plan.setPlanId(Ids.nextPlanId());
        }
        if (plan.getCreatedAt() == null) {
            plan.setCreatedAt(Instant.now());
        }
        plan.setUpdatedAt(Instant.now());
        if (plan.getStatus() == null) {
            plan.setStatus("active");
        }
        schemaMigrationService.applyDefaults(plan);
        return planJsonStore.save(plan);
    }

    @Override
    public TestPlan get(String planId) {
        return planJsonStore.get(planId);
    }

    @Override
    public List<TestPlan> list() {
        return planJsonStore.list();
    }

    @Override
    public TestPlan update(TestPlan plan) {
        plan.setUpdatedAt(Instant.now());
        schemaMigrationService.applyDefaults(plan);
        return planJsonStore.save(plan);
    }

    @Override
    public boolean archive(String planId) {
        TestPlan plan = get(planId);
        if (plan == null) {
            return false;
        }
        plan.setStatus("archived");
        update(plan);
        return true;
    }

    @Override
    public boolean delete(String planId) {
        return planJsonStore.delete(planId);
    }
}
