package io.github.newhoo.restkit.restful.detail;

import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.restful.RestItemDetail;
import io.github.newhoo.restkit.restful.ep.RestItemDetailProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static io.github.newhoo.restkit.common.RestConstant.PROTOCOL_HTTP;
import static io.github.newhoo.restkit.common.ToolkitIcons.DELETE;
import static io.github.newhoo.restkit.common.ToolkitIcons.GET;
import static io.github.newhoo.restkit.common.ToolkitIcons.PATCH;
import static io.github.newhoo.restkit.common.ToolkitIcons.POST;
import static io.github.newhoo.restkit.common.ToolkitIcons.PUT;
import static io.github.newhoo.restkit.common.ToolkitIcons.UNDEFINED;

/**
 * http restItm detail
 *
 * @author huzunrong
 * @since 3.0.0
 */
public class HttpRestItemDetail implements RestItemDetail {

    @NotNull
    @Override
    public String getProtocol() {
        return PROTOCOL_HTTP;
    }

    @Override
    public int getProtocolOrder() {
        return 0;
    }

    @NotNull
    @Override
    public Icon getIcon(@NotNull RestItem restItem) {
        HttpMethod method = restItem.getMethod();
        if (method == null) {
            return UNDEFINED;
        }
        switch (method) {
            case GET:
                return GET;
            case POST:
                return POST;
            case PUT:
                return PUT;
            case PATCH:
                return PATCH;
            case DELETE:
                return DELETE;
            default:
                return UNDEFINED;
        }
    }

    public static class HttpRestItemDetailProvider implements RestItemDetailProvider {
        @Override
        public HttpRestItemDetail createRestItemDetail() {
            return new HttpRestItemDetail();
        }
    }
}
