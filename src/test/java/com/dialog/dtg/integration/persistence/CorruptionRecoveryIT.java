package com.dialog.dtg.integration.persistence;

import com.dialog.dtg.core.store.RecoveryService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CorruptionRecoveryIT {

    @Test
    void shouldRestoreBackupWhenPrimaryFileIsCorrupted() throws Exception {
        RecoveryService recoveryService = new RecoveryService();
        Path temp = Files.createTempFile("recovery-test", ".json");
        Files.writeString(temp, "{\"valid\":true}");
        recoveryService.backup(temp);

        Files.writeString(temp, "{broken");
        assertTrue(recoveryService.restore(temp));
    }
}
