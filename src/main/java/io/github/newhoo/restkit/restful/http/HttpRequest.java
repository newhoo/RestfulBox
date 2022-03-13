package io.github.newhoo.restkit.restful.http;

import io.github.newhoo.restkit.common.Request;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * http request
 *
 * @author huzunrong
 * @date 2022/3/12 3:08 PM
 * @since 2.0.3
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpRequest extends Request {

    /** 原始http request */
    private org.apache.http.HttpRequest original;
}
