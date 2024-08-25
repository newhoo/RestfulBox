package io.github.newhoo.restkit.common;

import com.intellij.openapi.actionSystem.DataKey;

import java.util.List;

/**
 * data keys
 */
@NotProguard
public class RestDataKey {

    public static final DataKey<List<RestItem>> ALL_SERVICE = DataKey.create("ALL_SERVICE");

    public static final DataKey<String> SELECTED_PROJECT = DataKey.create("SELECTED_PROJECT");

    public static final DataKey<List<RestItem>> SELECTED_SERVICE = DataKey.create("SELECTED_SERVICE");

    public static final DataKey<List<RestItem>> SELECTED_PROJECT_SERVICE = DataKey.create("SELECTED_PROJECT_SERVICE");

    public static final DataKey<List<RestItem>> SELECTED_MODULE_SERVICE = DataKey.create("SELECTED_MODULE_SERVICE");

    public static final DataKey<List<RestItem>> SELECTED_PACKAGE_SERVICE = DataKey.create("SELECTED_PACKAGE_SERVICE");

    public static final DataKey<List<String>> ALL_PROJECT = DataKey.create("ALL_PROJECT");

    public static final DataKey<List<String>> ALL_MODULE = DataKey.create("ALL_MODULE");

    public static final DataKey<RestClientEditorInfo> CLIENT_EDITOR_INFO = DataKey.create("CLIENT_EDITOR_INFO");

    public static final DataKey<RestClientApiInfo> CLIENT_API_INFO = DataKey.create("CLIENT_API_INFO");
}