package com.dialog.dtg.core;

import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.model.TestRun;

public interface Executor {

    TestRun execute(TestPlan plan);
}
