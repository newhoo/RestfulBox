package io.github.newhoo.restkit.common;

import lombok.Data;
import org.apache.http.HttpResponse;

/**
 * Response
 *
 * @author huzunrong
 * @since 2.0.0
 */
@Data
public class Response {

    /** original http response */
    private HttpResponse original;

    /** response body */
    private String body;

    /** response original body */
    private String body0;

    public Response(HttpResponse original, String body) {
        this.original = original;
        this.body = body;
    }
}