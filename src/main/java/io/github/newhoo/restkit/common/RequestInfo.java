package io.github.newhoo.restkit.common;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * RequestInfo
 */
@NotProguard
@Getter
public class RequestInfo {

    private final Request request;
    private final Response response;

    @Setter
    private String remoteAddress;
    @Setter
    private long cost;
    @Setter
    private String errMsg;

    public RequestInfo(Request request, String errMsg) {
        this.request = request;
        this.response = null;
        this.errMsg = errMsg;
    }

    public RequestInfo(Request request, Response response, String remoteAddress, Long cost) {
        this.request = request;
        this.response = response;
        this.remoteAddress = remoteAddress;
        this.cost = cost;
    }

    public String getResponseBody() {
        return StringUtils.isNotEmpty(errMsg)
                ? errMsg
                : response != null ? StringUtils.defaultString(response.getBody()) : "";
    }
}