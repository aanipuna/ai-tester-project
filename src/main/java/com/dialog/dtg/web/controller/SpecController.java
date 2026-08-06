package com.dialog.dtg.web.controller;

import com.dialog.dtg.core.model.NormalizedSpec;
import com.dialog.dtg.core.service.WorkflowService;
import com.dialog.dtg.core.store.SpecStore;
import com.dialog.dtg.web.dto.SpecIngestResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/specs")
public class SpecController {

    private final WorkflowService workflowService;
    private final SpecStore specStore;

    public SpecController(WorkflowService workflowService, SpecStore specStore) {
        this.workflowService = workflowService;
        this.specStore = specStore;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SpecIngestResponse ingest(@RequestParam("sourceType") String sourceType,
                                     @RequestParam(value = "file", required = false) MultipartFile file,
                                     @RequestParam(value = "manualSpec", required = false) String manualSpec) throws IOException {
        String mode = sourceType.toLowerCase();
        Path temp;
        if ("manual".equals(mode)) {
            temp = Files.createTempFile("manual-spec", ".json");
            Files.writeString(temp, manualSpec == null ? "{}" : manualSpec, StandardCharsets.UTF_8);
        } else {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded file is required for sourceType=" + sourceType);
            }
            temp = Files.createTempFile("uploaded-spec", file.getOriginalFilename() == null ? ".tmp" : "-" + file.getOriginalFilename());
            file.transferTo(temp);
        }

        NormalizedSpec spec = workflowService.ingestSpec(mode, temp.toString());
        return new SpecIngestResponse(spec.getSpecId(), spec.getName());
    }

    @GetMapping("/{specId}")
    public NormalizedSpec get(@PathVariable String specId) {
        NormalizedSpec spec = specStore.get(specId);
        if (spec == null) {
            throw new IllegalArgumentException("Spec not found: " + specId);
        }
        return spec;
    }

    @GetMapping
    public List<NormalizedSpec> list() {
        return specStore.list();
    }
}
