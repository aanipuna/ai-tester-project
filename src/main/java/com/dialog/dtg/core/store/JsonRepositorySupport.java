package com.dialog.dtg.core.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import com.dialog.dtg.core.error.PersistenceException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonRepositorySupport {

    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper;
    private final AtomicFileWriter atomicFileWriter;
    private final FileLockManager lockManager;

    protected JsonRepositorySupport(ObjectMapper objectMapper,
                                    AtomicFileWriter atomicFileWriter,
                                    FileLockManager lockManager) {
        this.objectMapper = objectMapper;
        this.atomicFileWriter = atomicFileWriter;
        this.lockManager = lockManager;
    }

    protected <T> T read(Path path, Class<T> type) {
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return objectMapper.readValue(path.toFile(), type);
        } catch (IOException ex) {
            throw new PersistenceException("Failed to read JSON document: " + path, ex);
        }
    }

    protected void write(Path path, Object value) {
        try (FileLockManager.LockedHandle ignored = lockManager.lock(path, LOCK_TIMEOUT)) {
            byte[] payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(value);
            atomicFileWriter.writeAtomically(path, payload);
        } catch (IOException ex) {
            throw new PersistenceException("Failed to write JSON document: " + path, ex);
        }
    }

    protected boolean exists(Path path) {
        return Files.exists(path);
    }
}
