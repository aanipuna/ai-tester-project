package com.dialog.dtg.core;

import com.dialog.dtg.core.model.RunReport;
import com.dialog.dtg.core.model.TestRun;

public interface ReportBuilder {

    RunReport build(TestRun run);
}
