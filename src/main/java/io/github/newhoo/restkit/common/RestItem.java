package io.github.newhoo.restkit.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Restful Api Item
 */
@Data
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public class RestItem {

    private String url;
    private HttpMethod method;

    /** Request header list */
    private List<KV> headers = Collections.emptyList();

    /** Request params list */
    private List<KV> params = Collections.emptyList();

    /** Request body json */
    private String bodyJson;

    /** description will display in search everywhere item and tree item */
    private String description;


    // other info

    /** module name, as group */
    private String moduleName;

    /** name that the api item from. e.g. Local for Local api store */
    private String framework;

    /** id, reserve */
    private String id = "";

    /** timestamp */
    private long ts = System.currentTimeMillis();

    /**
     * Please do not call this constructor. It's called by deserialization framework such as io.github.newhoo.restkit.config.LocalApiLibrary
     */
    public RestItem() {
    }

    public RestItem(@NotNull String url, String requestMethod, @NotNull String description, @NotNull String moduleName, @NotNull String framework) {
        this.url = url;
        if (requestMethod != null) {
            method = HttpMethod.getByRequestMethod(requestMethod);
        }

        this.description = description;
        this.moduleName = moduleName;
        this.framework = framework;
    }

    public RestItem(@NotNull String url, String requestMethod, @NotNull List<KV> headers, @NotNull List<KV> params, @NotNull String bodyJson, @NotNull String description, @NotNull String moduleName, @NotNull String framework) {
        this(url, requestMethod, description, moduleName, framework);

        this.headers = headers;
        this.params = params;
        this.bodyJson = bodyJson;
    }

    public boolean isValid() {
        return true;
    }

    /**
     * for tree list operation: move、update, PsiRestItem should be false
     */
    public boolean canUpdate() {
        return true;
    }

    /**
     * for tree list operation: delete, PsiRestItem should be false
     */
    public boolean canDelete() {
        return true;
    }
}