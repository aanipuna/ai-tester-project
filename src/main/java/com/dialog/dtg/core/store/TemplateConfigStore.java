package com.dialog.dtg.core.store;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.PromptTemplateConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Repository
public class TemplateConfigStore {

    private static final String CONFIG_FILE = "prompt-templates.json";

    private final ObjectMapper objectMapper;
    private final AtomicFileWriter atomicFileWriter;
    private final DataPathProperties properties;

    public TemplateConfigStore(ObjectMapper objectMapper, AtomicFileWriter atomicFileWriter, DataPathProperties properties) {
        this.objectMapper = objectMapper;
        this.atomicFileWriter = atomicFileWriter;
        this.properties = properties;
    }

    public PromptTemplateConfig load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            return new PromptTemplateConfig();
        }
        try {
            return objectMapper.readValue(path.toFile(), PromptTemplateConfig.class);
        } catch (IOException ex) {
            return new PromptTemplateConfig();
        }
    }

    public void save(PromptTemplateConfig config) {
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(config);
            atomicFileWriter.writeAtomically(path, bytes);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save prompt templates", ex);
        }
    }

    private Path configPath() {
        return properties.getDataDir().resolve("config").resolve(CONFIG_FILE);
    }
}
