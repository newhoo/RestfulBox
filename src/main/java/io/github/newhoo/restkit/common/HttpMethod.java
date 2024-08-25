package io.github.newhoo.restkit.common;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP method
 */
@NotProguard
public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, UNDEFINED;

    private static final Map<String, HttpMethod> methodMap = new HashMap<>(8);

    public static HttpMethod getByRequestMethod(String method) {
        if (method == null || method.isEmpty()) {
            return UNDEFINED;
        }

        String[] split = method.split("\\.");

        if (split.length > 1) {
            method = split[split.length - 1].toUpperCase();
            return methodMap.getOrDefault(method.toUpperCase(), UNDEFINED);
        }

        return methodMap.getOrDefault(method.toUpperCase(), UNDEFINED);
    }

    public static HttpMethod nameOf(String method) {
        HttpMethod httpMethod = methodMap.get(method.toUpperCase());
        if (httpMethod == null) {
            return UNDEFINED;
//            throw new IllegalArgumentException("Not found enum constant in" + Arrays.toString(values()));
        }
        return httpMethod;
    }

    static {
        for (HttpMethod httpMethod : values()) {
            methodMap.put(httpMethod.name(), httpMethod);
        }
    }
}