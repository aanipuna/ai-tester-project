package com.dialog.dtg.core.store;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.WorkflowRun;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WorkflowRunStore extends JsonRepositorySupport {

    private static final String RESULTS_FILE = "results.json";

    private final DataPathProperties properties;

    public WorkflowRunStore(ObjectMapper objectMapper, AtomicFileWriter atomicFileWriter,
                            FileLockManager lockManager, DataPathProperties properties) {
        super(objectMapper, atomicFileWriter, lockManager);
        this.properties = properties;
    }

    public WorkflowRun save(WorkflowRun run) {
        write(resolveResultsPath(run.getWorkflowRunId()), run);
        return run;
    }

    public WorkflowRun get(String runId) {
        return read(resolveResultsPath(runId), WorkflowRun.class);
    }

    public List<WorkflowRun> list() {
        List<WorkflowRun> out = new ArrayList<>();
        Path dir = properties.workflowRunsDir();
        if (!Files.exists(dir)) return out;
        try {
            Files.list(dir)
                .filter(Files::isDirectory)
                .forEach(p -> {
                    WorkflowRun run = read(p.resolve(RESULTS_FILE), WorkflowRun.class);
                    if (run != null) out.add(run);
                });
        } catch (IOException ignored) {}
        return out;
    }

    public boolean delete(String runId) {
        Path dir = runDirectory(runId);
        try {
            if (!Files.exists(dir)) return false;
            Files.walk(dir).sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            return true;
        } catch (IOException ex) { return false; }
    }

    public Path runDirectory(String runId) {
        return properties.workflowRunsDir().resolve(runId);
    }

    private Path resolveResultsPath(String runId) {
        return runDirectory(runId).resolve(RESULTS_FILE);
    }
}
