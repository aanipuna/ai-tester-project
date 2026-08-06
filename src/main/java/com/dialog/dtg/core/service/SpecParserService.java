package com.dialog.dtg.core.service;

import com.dialog.dtg.core.SpecParser;
import com.dialog.dtg.core.model.EndpointSpec;
import com.dialog.dtg.core.model.NormalizedSpec;
import com.dialog.dtg.core.model.ParameterSpec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SpecParserService implements SpecParser {

    private final ObjectMapper objectMapper;

    public SpecParserService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public NormalizedSpec parseFromOpenApi(String sourcePath) {
        try {
            List<String> lines = Files.readAllLines(Path.of(sourcePath));
            String title = "OpenAPI Imported Spec";
            String baseUrl = "http://localhost";
            String currentPath = null;
            List<EndpointSpec> endpoints = new ArrayList<>();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.startsWith("title:")) {
                    title = line.substring("title:".length()).trim();
                }
                if (line.startsWith("- url:")) {
                    baseUrl = line.substring("- url:".length()).trim();
                }
                if (line.startsWith("/") && line.endsWith(":")) {
                    currentPath = line.substring(0, line.length() - 1);
                } else if (currentPath != null && line.endsWith(":") && isHttpMethod(line.substring(0, line.length() - 1))) {
                    String method = line.substring(0, line.length() - 1).toUpperCase(Locale.ROOT);
                    EndpointSpec endpoint = new EndpointSpec();
                    endpoint.setEndpointId(method + "-" + currentPath.replace('/', '-'));
                    endpoint.setMethod(method);
                    endpoint.setPath(currentPath);
                    endpoint.setAuthType("none");
                    endpoint.setExpectedSuccessStatus(method.equals("POST") ? 201 : 200);
                    endpoint.setParameters(new ArrayList<>());
                    endpoints.add(endpoint);
                }
            }

            if (endpoints.isEmpty()) {
                EndpointSpec fallback = new EndpointSpec();
                fallback.setEndpointId("GET-root");
                fallback.setMethod("GET");
                fallback.setPath("/");
                fallback.setAuthType("none");
                fallback.setExpectedSuccessStatus(200);
                fallback.setParameters(new ArrayList<>());
                endpoints.add(fallback);
            }

            NormalizedSpec spec = new NormalizedSpec();
            spec.setSpecId(Ids.nextSpecId());
            spec.setSourceType("openapi");
            spec.setName(title);
            spec.setBaseUrl(baseUrl);
            spec.setImportedAt(Instant.now());
            spec.setEndpoints(endpoints);
            return spec;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to parse OpenAPI spec: " + sourcePath, ex);
        }
    }

    @Override
    public NormalizedSpec parseFromPostman(String sourcePath) {
        try {
            Map<String, Object> root = objectMapper.readValue(Path.of(sourcePath).toFile(), new TypeReference<>() {});
            String name = String.valueOf(root.getOrDefault("name", "Postman Imported Spec"));
            List<EndpointSpec> endpoints = new ArrayList<>();
            String derivedBaseUrl = "http://localhost";
            Object item = root.get("item");
            if (item instanceof List<?> items) {
                for (Object o : items) {
                    if (o instanceof Map<?, ?> map && map.get("request") instanceof Map<?, ?> req) {
                        EndpointSpec endpoint = new EndpointSpec();
                        Object methodRaw = req.get("method");
                        String method = (methodRaw != null ? methodRaw.toString() : "GET").toUpperCase(Locale.ROOT);
                        endpoint.setMethod(method);

                        // Extract base URL and path from url object or raw string
                        String path = "/";
                        Object urlObj = req.get("url");
                        if (urlObj instanceof Map<?, ?> urlMap) {
                            Object hostObj = urlMap.get("host");
                            Object pathObj = urlMap.get("path");
                            Object rawObj = urlMap.get("raw");
                            String host = hostObj instanceof List<?> hl
                                    ? String.join(".", hl.stream().map(Object::toString).toList())
                                    : (hostObj != null ? hostObj.toString() : null);
                            if (host != null && !host.isBlank()) {
                                derivedBaseUrl = (host.startsWith("http") ? "" : "https://") + host;
                            }
                            if (pathObj instanceof List<?> pl) {
                                path = "/" + pl.stream().map(Object::toString).collect(java.util.stream.Collectors.joining("/"));
                            } else if (rawObj != null) {
                                String raw = rawObj.toString();
                                int slashIdx = raw.indexOf('/');
                                path = slashIdx >= 0 ? raw.substring(slashIdx) : "/" + raw;
                            }
                        } else if (urlObj instanceof String urlStr) {
                            int slashIdx = urlStr.indexOf('/');
                            if (slashIdx > 0) {
                                String hostPart = urlStr.substring(0, slashIdx);
                                derivedBaseUrl = (hostPart.startsWith("http") ? "" : "https://") + hostPart;
                                path = urlStr.substring(slashIdx);
                            }
                        }

                        endpoint.setPath(path);
                        endpoint.setEndpointId(method + "-" + path.replace('/', '-').replaceAll("^-", ""));
                        endpoint.setExpectedSuccessStatus(method.equals("POST") ? 201 : 200);
                        endpoint.setAuthType("none");
                        endpoint.setParameters(new ArrayList<>());
                        endpoints.add(endpoint);
                    }
                }
            }
            if (endpoints.isEmpty()) {
                EndpointSpec endpoint = new EndpointSpec();
                endpoint.setEndpointId("GET-fallback");
                endpoint.setMethod("GET");
                endpoint.setPath("/");
                endpoint.setExpectedSuccessStatus(200);
                endpoint.setAuthType("none");
                endpoint.setParameters(new ArrayList<>());
                endpoints.add(endpoint);
            }

            NormalizedSpec spec = new NormalizedSpec();
            spec.setSpecId(Ids.nextSpecId());
            spec.setSourceType("postman");
            spec.setName(name);
            spec.setBaseUrl(derivedBaseUrl);
            spec.setImportedAt(Instant.now());
            spec.setEndpoints(endpoints);
            return spec;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to parse Postman collection: " + sourcePath, ex);
        }
    }

    @Override
    public NormalizedSpec parseFromManualJson(String sourcePath) {
        try {
            Map<String, Object> root = objectMapper.readValue(Path.of(sourcePath).toFile(), new TypeReference<>() {});
            NormalizedSpec spec = new NormalizedSpec();
            spec.setSpecId(Ids.nextSpecId());
            spec.setSourceType("manual");
            spec.setName(String.valueOf(root.getOrDefault("name", "Manual Spec")));
            spec.setBaseUrl(String.valueOf(root.getOrDefault("baseUrl", "http://localhost")));
            spec.setImportedAt(Instant.now());

            EndpointSpec endpoint = new EndpointSpec();
            endpoint.setMethod(String.valueOf(root.getOrDefault("method", "GET")).toUpperCase(Locale.ROOT));
            endpoint.setPath(String.valueOf(root.getOrDefault("endpoint", "/")));
            endpoint.setEndpointId(endpoint.getMethod() + "-" + endpoint.getPath().replace('/', '-'));
            endpoint.setExpectedSuccessStatus(Integer.parseInt(String.valueOf(root.getOrDefault("expectedStatus", 200))));
            endpoint.setAuthType(String.valueOf(root.getOrDefault("authType", "none")));
            endpoint.setParameters(new ArrayList<>());

            Object params = root.get("parameters");
            if (params instanceof List<?> list) {
                List<ParameterSpec> parameterSpecs = new ArrayList<>();
                for (Object entry : list) {
                    if (entry instanceof Map<?, ?> m) {
                        ParameterSpec p = new ParameterSpec();
                            Object nameVal = m.get("name"); p.setName(nameVal != null ? nameVal.toString() : "param");
                            Object typeVal = m.get("type"); p.setDataType(typeVal != null ? typeVal.toString() : "string");
                            Object locVal = m.get("location"); p.setLocation(locVal != null ? locVal.toString() : "query");
                            Object reqVal = m.get("required"); p.setRequired(Boolean.parseBoolean(reqVal != null ? reqVal.toString() : "false"));
                        parameterSpecs.add(p);
                    }
                }
                endpoint.setParameters(parameterSpecs);
            }

            spec.setEndpoints(List.of(endpoint));
            return spec;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to parse manual spec JSON: " + sourcePath, ex);
        }
    }

    private boolean isHttpMethod(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.equals("get") || lower.equals("post") || lower.equals("put")
            || lower.equals("patch") || lower.equals("delete") || lower.equals("head") || lower.equals("options");
    }
}
