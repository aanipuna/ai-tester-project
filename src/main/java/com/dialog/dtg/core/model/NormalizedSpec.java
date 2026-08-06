package com.dialog.dtg.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class NormalizedSpec {

    private String schemaVersion;
    private String specId;
    private String sourceType;
    private String name;
    private String baseUrl;
    private List<EndpointSpec> endpoints = new ArrayList<>();
    private Instant importedAt;
    private List<String> tags = new ArrayList<>();

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getSpecId() {
        return specId;
    }

    public void setSpecId(String specId) {
        this.specId = specId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<EndpointSpec> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<EndpointSpec> endpoints) {
        this.endpoints = endpoints;
    }

    public Instant getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(Instant importedAt) {
        this.importedAt = importedAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
