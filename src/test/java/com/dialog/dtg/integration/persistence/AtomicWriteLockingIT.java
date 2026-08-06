package com.dialog.dtg.integration.persistence;

import com.dialog.dtg.core.store.AtomicFileWriter;
import com.dialog.dtg.core.store.FileLockManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AtomicWriteLockingIT {

    @Test
    void shouldWriteFileAtomicallyAndAcquireLock() throws Exception {
        Path temp = Files.createTempFile("atomic-test", ".json");
        AtomicFileWriter writer = new AtomicFileWriter();
        writer.writeAtomically(temp, "{\"ok\":true}".getBytes());
        assertTrue(Files.size(temp) > 0);

        FileLockManager lockManager = new FileLockManager();
        try (var ignored = lockManager.lock(temp, Duration.ofSeconds(1))) {
            assertTrue(true);
        }
    }
}
