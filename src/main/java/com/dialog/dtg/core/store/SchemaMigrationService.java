package com.dialog.dtg.core.store;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.NormalizedSpec;
import com.dialog.dtg.core.model.TestPlan;
import com.dialog.dtg.core.model.TestRun;
import org.springframework.stereotype.Service;

@Service
public class SchemaMigrationService {

    private final DataPathProperties properties;

    public SchemaMigrationService(DataPathProperties properties) {
        this.properties = properties;
    }

    public void applyDefaults(NormalizedSpec spec) {
        if (spec.getSchemaVersion() == null || spec.getSchemaVersion().isBlank()) {
            spec.setSchemaVersion(properties.getStorage().getSchemaVersion());
        }
    }

    public void applyDefaults(TestPlan plan) {
        if (plan.getSchemaVersion() == null || plan.getSchemaVersion().isBlank()) {
            plan.setSchemaVersion(properties.getStorage().getSchemaVersion());
        }
    }

    public void applyDefaults(TestRun run) {
        if (run.getSchemaVersion() == null || run.getSchemaVersion().isBlank()) {
            run.setSchemaVersion(properties.getStorage().getSchemaVersion());
        }
    }
}
