package com.dialog.dtg.core.store;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.AuthConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Repository
public class AuthConfigStore {

    private static final String CONFIG_FILE = "auth-config.json";

    private final ObjectMapper objectMapper;
    private final AtomicFileWriter atomicFileWriter;
    private final DataPathProperties properties;

    public AuthConfigStore(ObjectMapper objectMapper, AtomicFileWriter atomicFileWriter, DataPathProperties properties) {
        this.objectMapper = objectMapper;
        this.atomicFileWriter = atomicFileWriter;
        this.properties = properties;
    }

    public AuthConfig load() {
        Path path = configPath();
        if (!Files.exists(path)) return new AuthConfig();
        try {
            return objectMapper.readValue(path.toFile(), AuthConfig.class);
        } catch (IOException ex) {
            return new AuthConfig();
        }
    }

    public void save(AuthConfig config) {
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(config);
            atomicFileWriter.writeAtomically(path, bytes);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save auth config", ex);
        }
    }

    private Path configPath() {
        return properties.getDataDir().resolve(CONFIG_FILE);
    }
}
