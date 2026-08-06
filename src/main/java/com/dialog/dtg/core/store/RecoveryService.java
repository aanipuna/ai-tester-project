package com.dialog.dtg.core.store;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class RecoveryService {

    public Path backupFile(Path path) {
        return path.resolveSibling(path.getFileName().toString() + ".bak");
    }

    public void backup(Path path) {
        Path backup = backupFile(path);
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    public boolean restore(Path path) {
        Path backup = backupFile(path);
        if (!Files.exists(backup)) {
            return false;
        }
        try {
            Files.copy(backup, path, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
