package io.github.newhoo.restkit.restful.http;

import io.github.newhoo.restkit.common.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.stream.Collectors;

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

        setCode(original.getStatusLine().getStatusCode());
        setHeaders(Arrays.stream(original.getAllHeaders())
                         .collect(Collectors.toMap(org.apache.http.NameValuePair::getName, org.apache.http.NameValuePair::getValue)));
        setBody(body);
    }
}
