package io.github.newhoo.restkit.common;

import lombok.Data;

import java.util.Map;

/**
 * Response
 *
 * @author huzunrong
 * @since 2.0.0
 */
@Data
public class Response {

    /** response code */
    private int code;

    /** response headers */
    private Map<String, String> headers;

    /** response body */
    private String body;
}