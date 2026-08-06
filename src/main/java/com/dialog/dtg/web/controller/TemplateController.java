package com.dialog.dtg.web.controller;

import com.dialog.dtg.core.model.PromptTemplateConfig;
import com.dialog.dtg.core.store.TemplateConfigStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateConfigStore templateConfigStore;

    public TemplateController(TemplateConfigStore templateConfigStore) {
        this.templateConfigStore = templateConfigStore;
    }

    @GetMapping
    public PromptTemplateConfig get() {
        return templateConfigStore.load();
    }

    @PutMapping
    public PromptTemplateConfig update(@RequestBody PromptTemplateConfig config) {
        templateConfigStore.save(config);
        return templateConfigStore.load();
    }
}
