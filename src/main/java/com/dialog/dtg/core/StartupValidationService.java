package com.dialog.dtg.core;

import com.dialog.dtg.core.config.DataPathProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
public class StartupValidationService {

    private final DataPathProperties properties;

    public StartupValidationService(DataPathProperties properties) {
        this.properties = properties;
    }

    public void validateAndInitialize() {
        try {
            Files.createDirectories(properties.getDataDir());
            Files.createDirectories(properties.specsDir());
            Files.createDirectories(properties.plansDir());
            Files.createDirectories(properties.runsDir());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize data directories", ex);
        }
    }
}
