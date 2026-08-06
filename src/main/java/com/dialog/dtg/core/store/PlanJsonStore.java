package com.dialog.dtg.core.store;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.TestPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PlanJsonStore extends JsonRepositorySupport {

    private final DataPathProperties properties;

    public PlanJsonStore(ObjectMapper objectMapper, AtomicFileWriter atomicFileWriter, FileLockManager lockManager, DataPathProperties properties) {
        super(objectMapper, atomicFileWriter, lockManager);
        this.properties = properties;
    }

    public TestPlan save(TestPlan plan) {
        write(resolvePath(plan.getPlanId()), plan);
        return plan;
    }

    public TestPlan get(String planId) {
        String normalized = planId.endsWith(".json") ? planId.substring(0, planId.length() - 5) : planId;
        return read(resolvePath(normalized), TestPlan.class);
    }

    public List<TestPlan> list() {
        List<TestPlan> out = new ArrayList<>();
        Path dir = properties.plansDir();
        if (!Files.exists(dir)) {
            return out;
        }
        try {
            Files.list(dir)
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .forEach(p -> {
                    TestPlan plan = read(p, TestPlan.class);
                    if (plan != null) {
                        out.add(plan);
                    }
                });
        } catch (IOException ignored) {
            return out;
        }
        return out;
    }

    public boolean delete(String planId) {
        Path path = resolvePath(planId);
        try {
            return Files.deleteIfExists(path);
        } catch (IOException ex) {
            return false;
        }
    }

    private Path resolvePath(String planId) {
        return properties.plansDir().resolve(planId + ".json");
    }
}
