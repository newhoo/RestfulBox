package io.github.newhoo.restkit.common;

import lombok.Data;
import org.apache.http.HttpRequest;

import java.util.Map;

/**
 * Request
 *
 * @author huzunrong
 * @since 2.0.0
 */
@Data
public class Request {

    /** 原始http request */
    private HttpRequest original;

    private String url;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> params;
    private String body;
}