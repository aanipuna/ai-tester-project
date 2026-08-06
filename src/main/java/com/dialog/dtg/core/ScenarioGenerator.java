package com.dialog.dtg.core;

import com.dialog.dtg.core.model.EndpointSpec;
import com.dialog.dtg.core.model.TestPlan;

public interface ScenarioGenerator {

    TestPlan generatePlan(EndpointSpec endpointSpec);
}
