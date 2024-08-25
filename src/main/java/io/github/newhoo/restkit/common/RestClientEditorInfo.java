package io.github.newhoo.restkit.common;

import lombok.Data;

/**
 * tree editor info
 *
 * @author huzunrong
 * @since 1.0.8
 */
@NotProguard
@Data
public class RestClientEditorInfo {

    private String project;

    private String method;

    private String url;

    private String editor;

    public String getParameterKey() {
        return method + "_" + url;
    }
}
