package io.github.newhoo.restkit.common;

import lombok.Data;

/**
 * @author huzunrong
 * @since 1.0.8
 */
@NotProguard
@Data
public class RestClientApiInfo {

    private String project;

    private String currentEnv;

    private String url;

    private HttpMethod method;

    /**
     * Request header list
     * <p>
     * 格式：英文冒号分割
     * <pre>
     *     header1: value1
     *     header2: value2
     * </pre>
     */
    private String headers;

    /**
     * Request params list
     * <p>
     * 格式：英文冒号分割
     * <pre>
     *     key1: value1
     *     key2: value2
     * </pre>
     */
    private String params;

    /**
     * Request body json
     */
    private String bodyJson;

    /**
     * Request config list
     * <p>
     * 格式：英文冒号分割
     * <pre>
     *     key1: value1
     *     key2: value2
     * </pre>
     */
    private String config;
}
