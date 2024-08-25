package io.github.newhoo.restkit.restful.http;

import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.Request;
import lombok.EqualsAndHashCode;

/**
 * http request
 *
 * @author huzunrong
 * @date 2022/3/12 3:08 PM
 * @since 2.0.3
 */
@EqualsAndHashCode(callSuper = true)
public class HttpRequest extends Request {

    /** 原始http request */
    @NotProguard
    private org.apache.http.HttpRequest original;

    @NotProguard
    public org.apache.http.HttpRequest getOriginal() {
        return original;
    }

    @NotProguard
    public void setOriginal(org.apache.http.HttpRequest original) {
        this.original = original;
    }
}
