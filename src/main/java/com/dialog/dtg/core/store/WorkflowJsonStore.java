package com.dialog.dtg.core.store;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.Workflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WorkflowJsonStore extends JsonRepositorySupport {

    private final DataPathProperties properties;

    public WorkflowJsonStore(ObjectMapper objectMapper, AtomicFileWriter atomicFileWriter,
                             FileLockManager lockManager, DataPathProperties properties) {
        super(objectMapper, atomicFileWriter, lockManager);
        this.properties = properties;
    }

    public Workflow save(Workflow workflow) {
        write(resolvePath(workflow.getWorkflowId()), workflow);
        return workflow;
    }

    public Workflow get(String workflowId) {
        return read(resolvePath(workflowId), Workflow.class);
    }

    public List<Workflow> list() {
        List<Workflow> out = new ArrayList<>();
        Path dir = properties.workflowsDir();
        if (!Files.exists(dir)) return out;
        try {
            Files.list(dir)
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .forEach(p -> {
                    Workflow w = read(p, Workflow.class);
                    if (w != null) out.add(w);
                });
        } catch (IOException ignored) {}
        return out;
    }

    public boolean delete(String workflowId) {
        try {
            return Files.deleteIfExists(resolvePath(workflowId));
        } catch (IOException ex) {
            return false;
        }
    }

    private Path resolvePath(String workflowId) {
        return properties.workflowsDir().resolve(workflowId + ".json");
    }
}
