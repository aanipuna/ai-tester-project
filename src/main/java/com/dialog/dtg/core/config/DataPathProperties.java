package com.dialog.dtg.core.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "api-test-agent")
public class DataPathProperties {

    private Path dataDir = Paths.get("./data");

    private final Storage storage = new Storage();

    public Path getDataDir() {
        return dataDir;
    }

    public void setDataDir(Path dataDir) {
        this.dataDir = dataDir;
    }

    public Storage getStorage() {
        return storage;
    }

    public Path specsDir() {
        return dataDir.resolve("specs");
    }

    public Path plansDir() {
        return dataDir.resolve("plans");
    }

    public Path runsDir() {
        return dataDir.resolve("runs");
    }

    public static class Storage {

        @NotBlank
        private String schemaVersion = "1.0";

        public String getSchemaVersion() {
            return schemaVersion;
        }

        public void setSchemaVersion(String schemaVersion) {
            this.schemaVersion = schemaVersion;
        }
    }
}
