package com.dialog.dtg.web.controller;

import com.dialog.dtg.core.model.AuthConfig;
import com.dialog.dtg.core.store.AuthConfigStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth-config")
public class AuthConfigController {

    private final AuthConfigStore authConfigStore;

    public AuthConfigController(AuthConfigStore authConfigStore) {
        this.authConfigStore = authConfigStore;
    }

    @GetMapping
    public AuthConfig get() {
        return authConfigStore.load();
    }

    @PostMapping
    public AuthConfig save(@RequestBody AuthConfig config) {
        authConfigStore.save(config);
        return config;
    }
}
