package com.dialog.dtg.core;

import java.util.List;

import com.dialog.dtg.core.model.TestPlan;

public interface PlanStore {

    TestPlan create(TestPlan plan);

    TestPlan get(String planId);

    List<TestPlan> list();

    TestPlan update(TestPlan plan);

    boolean archive(String planId);
}
