package com.dialog.dtg.web.dto;

public class SpecIngestResponse {

    private String specId;
    private String name;

    public SpecIngestResponse() {
    }

    public SpecIngestResponse(String specId, String name) {
        this.specId = specId;
        this.name = name;
    }

    public String getSpecId() {
        return specId;
    }

    public void setSpecId(String specId) {
        this.specId = specId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
