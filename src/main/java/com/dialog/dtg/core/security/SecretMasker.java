package com.dialog.dtg.core.security;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class SecretMasker {

    private static final Set<String> SENSITIVE_KEYS = new HashSet<>();

    static {
        SENSITIVE_KEYS.add("authorization");
        SENSITIVE_KEYS.add("token");
        SENSITIVE_KEYS.add("api-key");
        SENSITIVE_KEYS.add("apikey");
        SENSITIVE_KEYS.add("password");
        SENSITIVE_KEYS.add("secret");
    }

    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?i)(token|authorization|api[-_]?key|password|secret)\\s*[:=]\\s*[^,\\s}]+");

    private SecretMasker() {
    }

    public static String maskValue(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    public static String maskIfSensitive(String key, String value) {
        if (key == null) {
            return value;
        }
        return isSensitiveKey(key) ? maskValue(value) : value;
    }

    public static void maskHeadersInPlace(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        headers.replaceAll(SecretMasker::maskIfSensitive);
    }

    public static String maskSensitiveText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return TOKEN_PATTERN.matcher(text).replaceAll("$1=****");
    }

    private static boolean isSensitiveKey(String key) {
        return SENSITIVE_KEYS.contains(key.toLowerCase());
    }
}
