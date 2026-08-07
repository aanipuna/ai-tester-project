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
            Map<String, Object> info = root.get("info") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
            String name = String.valueOf(info.getOrDefault("name", root.getOrDefault("name", "Postman Imported Spec")));

            // Build variable map for resolving {{VAR}} placeholders
            Map<String, String> variables = new java.util.HashMap<>();
            Object varList = root.get("variable");
            if (varList instanceof List<?> vl) {
                for (Object v : vl) {
                    if (v instanceof Map<?, ?> vm) {
                        Object k = vm.get("key"); Object val = vm.get("value");
                        if (k != null && val != null) variables.put(k.toString(), val.toString());
                    }
                }
            }

            List<EndpointSpec> endpoints = new ArrayList<>();
            String[] derivedBaseUrl = {"http://localhost"};

            // Recursively collect all request items from nested folders
            collectEndpoints(root.get("item"), variables, endpoints, derivedBaseUrl);

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
            spec.setBaseUrl(derivedBaseUrl[0]);
            spec.setImportedAt(Instant.now());
            spec.setEndpoints(endpoints);
            return spec;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to parse Postman collection: " + sourcePath, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void collectEndpoints(Object itemList, Map<String, String> variables,
                                  List<EndpointSpec> endpoints, String[] derivedBaseUrl) {
        if (!(itemList instanceof List<?> items)) return;
        for (Object o : items) {
            if (!(o instanceof Map<?, ?> map)) continue;
            if (map.get("item") != null) {
                // Folder — recurse into it
                collectEndpoints(map.get("item"), variables, endpoints, derivedBaseUrl);
            } else if (map.get("request") instanceof Map<?, ?> req) {
                EndpointSpec endpoint = buildEndpoint(req, variables, derivedBaseUrl);
                if (endpoint != null) endpoints.add(endpoint);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private EndpointSpec buildEndpoint(Map<?, ?> req, Map<String, String> variables, String[] derivedBaseUrl) {
        EndpointSpec endpoint = new EndpointSpec();
        Object methodRaw = req.get("method");
        String method = (methodRaw != null ? methodRaw.toString() : "GET").toUpperCase(Locale.ROOT);
        endpoint.setMethod(method);

        String path = "/";
        Object urlObj = req.get("url");
        if (urlObj instanceof Map<?, ?> urlMap) {
            Object rawObj = urlMap.get("raw");
            Object pathObj = urlMap.get("path");
            Object hostObj = urlMap.get("host");

            // Resolve raw URL by substituting {{VARIABLE}} with known values
            String rawUrl = rawObj != null ? resolveVars(rawObj.toString(), variables) : null;

            // Derive base URL from resolved raw URL
            if (rawUrl != null && rawUrl.startsWith("http")) {
                try {
                    java.net.URI uri = new java.net.URI(rawUrl);
                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    int port = uri.getPort();
                    if (host != null) {
                        derivedBaseUrl[0] = scheme + "://" + host + (port > 0 ? ":" + port : "");
                        path = uri.getPath();
                        if (uri.getQuery() != null) path += "?" + uri.getQuery();
                        if (path.isBlank()) path = "/";
                    }
                } catch (Exception ignored) {}
            }

            // Fall back to host + path arrays
            if (path.equals("/") && hostObj instanceof List<?> hl) {
                String host = hl.stream().map(Object::toString)
                    .map(h -> resolveVars(h, variables))
                    .collect(java.util.stream.Collectors.joining("."));
                if (!host.isBlank()) {
                    derivedBaseUrl[0] = (host.startsWith("http") ? "" : "https://") + host;
                }
            }
            if (path.equals("/") && pathObj instanceof List<?> pl) {
                path = "/" + pl.stream()
                    .map(p -> resolveVars(p.toString(), variables))
                    .collect(java.util.stream.Collectors.joining("/"));
            }
        } else if (urlObj instanceof String urlStr) {
            String resolved = resolveVars(urlStr, variables);
            if (resolved.startsWith("http")) {
                try {
                    java.net.URI uri = new java.net.URI(resolved);
                    derivedBaseUrl[0] = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
                    path = uri.getPath();
                    if (uri.getQuery() != null) path += "?" + uri.getQuery();
                    if (path.isBlank()) path = "/";
                } catch (Exception ignored) {}
            }
        }

        // Normalize trailing slash on base URL
        if (derivedBaseUrl[0].endsWith("/")) derivedBaseUrl[0] = derivedBaseUrl[0].substring(0, derivedBaseUrl[0].length() - 1);

        endpoint.setPath(path.isBlank() ? "/" : (path.startsWith("//") ? path.substring(1) : path));
        endpoint.setEndpointId(method + "-" + path.replace('/', '-').replaceAll("^-|-$", "").replaceAll("[^a-zA-Z0-9_-]", "_"));
        endpoint.setExpectedSuccessStatus(method.equals("POST") ? 201 : 200);

        // Detect auth type from headers
        String authType = "none";
        Object headersObj = req.get("header");
        if (headersObj instanceof List<?> headers) {
            for (Object h : headers) {
                if (h instanceof Map<?, ?> hm) {
                    String key = String.valueOf(hm.get("key"));
                    if ("Authorization".equalsIgnoreCase(key) || "token".equalsIgnoreCase(key)) {
                        authType = "bearer";
                        break;
                    }
                }
            }
        }
        endpoint.setAuthType(authType);

        // Extract body parameters
        List<ParameterSpec> params = new ArrayList<>();
        Object bodyObj = req.get("body");
        if (bodyObj instanceof Map<?, ?> body) {
            Object mode = body.get("mode");
            if ("raw".equals(mode)) {
                Object rawBody = body.get("raw");
                if (rawBody instanceof String rawStr && !rawStr.isBlank()) {
                    try {
                        Map<?, ?> bodyMap = objectMapper.readValue(rawStr, Map.class);
                        for (Map.Entry<?, ?> entry : bodyMap.entrySet()) {
                            ParameterSpec p = new ParameterSpec();
                            p.setName(entry.getKey().toString());
                            p.setLocation("body");
                            p.setRequired(true);
                            p.setDataType(entry.getValue() instanceof Number ? "number" : "string");
                            params.add(p);
                        }
                    } catch (Exception ignored) {}
                }
            } else if ("urlencoded".equals(mode) && body.get("urlencoded") instanceof List<?> ul) {
                for (Object u : ul) {
                    if (u instanceof Map<?, ?> um) {
                        ParameterSpec p = new ParameterSpec();
                        Object keyVal = um.get("key"); Object disabledVal = um.get("disabled");
                        p.setName(keyVal != null ? keyVal.toString() : "param");
                        p.setLocation("body");
                        p.setRequired(!Boolean.parseBoolean(disabledVal != null ? disabledVal.toString() : "false"));
                        p.setDataType("string");
                        params.add(p);
                    }
                }
            } else if ("formdata".equals(mode) && body.get("formdata") instanceof List<?> fl) {
                for (Object f : fl) {
                    if (f instanceof Map<?, ?> fm) {
                        ParameterSpec p = new ParameterSpec();
                        Object keyVal = fm.get("key"); Object disabledVal = fm.get("disabled");
                        p.setName(keyVal != null ? keyVal.toString() : "param");
                        p.setLocation("form");
                        p.setRequired(!Boolean.parseBoolean(disabledVal != null ? disabledVal.toString() : "false"));
                        p.setDataType("string");
                        params.add(p);
                    }
                }
            }
        }
        endpoint.setParameters(params);
        return endpoint;
    }

    /** Replace all {{VAR}} placeholders with values from the variable map. */
    private String resolveVars(String raw, Map<String, String> vars) {
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            raw = raw.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return raw;
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
