package io.github.newhoo.restkit.common;

import lombok.Data;

import java.util.Map;

/**
 * rest client info
 *
 * @author huzunrong
 * @since 2.0.0
 */
@Data
public class RestClientData {

    private String url;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> params;
    private String body;
}