package com.dialog.dtg.core.store;

import com.dialog.dtg.core.config.DataPathProperties;
import com.dialog.dtg.core.model.NormalizedSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SpecStore extends JsonRepositorySupport {

    private final DataPathProperties properties;

    public SpecStore(ObjectMapper objectMapper, AtomicFileWriter atomicFileWriter, FileLockManager lockManager, DataPathProperties properties) {
        super(objectMapper, atomicFileWriter, lockManager);
        this.properties = properties;
    }

    public NormalizedSpec save(NormalizedSpec spec) {
        Path path = resolvePath(spec.getSpecId());
        write(path, spec);
        return spec;
    }

    public NormalizedSpec get(String specId) {
        return read(resolvePath(specId), NormalizedSpec.class);
    }

    public List<NormalizedSpec> list() {
        List<NormalizedSpec> out = new ArrayList<>();
        Path dir = properties.specsDir();
        if (!Files.exists(dir)) {
            return out;
        }
        try {
            Files.list(dir)
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .forEach(p -> {
                    NormalizedSpec spec = read(p, NormalizedSpec.class);
                    if (spec != null) {
                        out.add(spec);
                    }
                });
        } catch (IOException ignored) {
            return out;
        }
        return out;
    }

    private Path resolvePath(String specId) {
        return properties.specsDir().resolve(specId + ".json");
    }
}
