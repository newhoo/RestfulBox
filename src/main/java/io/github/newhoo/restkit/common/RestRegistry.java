package io.github.newhoo.restkit.common;

import com.intellij.openapi.util.registry.Registry;

public class RestRegistry {

    public static boolean debugMode() {
        return Registry.is("restkit.mode.debug", false);
    }

    public static boolean enableLocalApi() {
        return Registry.is("restkit.localApi.enable", false);
    }
}
