package io.github.newhoo.restkit.restful.http;

import io.github.newhoo.restkit.common.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * http response
 *
 * @author huzunrong
 * @date 2022/3/12 3:10 PM
 * @since 2.0.3
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpResponse extends Response {

    /** original http response */
    private org.apache.http.HttpResponse original;

    /** response original body */
    private String body0;

    public HttpResponse(org.apache.http.HttpResponse original, String body) {
        this.original = original;
        this.body0 = body;

        setBody(body);
    }
}
