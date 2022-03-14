package io.github.newhoo.restkit.restful;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.RequestInfo;
import io.github.newhoo.restkit.common.RestClientData;
import org.jetbrains.annotations.NotNull;

/**
 * rest client extension point
 *
 * @author huzunrong
 * @since 2.0.3
 */
public interface RestClient {

    /**
     * supported protocol
     */
    @NotNull
    String getProtocol();

    /**
     * 创建请求
     *
     * @param restClientData
     * @param project
     */
    @NotNull
    Request createRequest(RestClientData restClientData, Project project);

    /**
     * 发送请求
     *
     * @param request created by #createRequest()
     */
    @NotNull
    RequestInfo sendRequest(Request request, Project project);

    /**
     * 格式化rest client中的Response
     *
     * @param requestInfo
     */
    @NotNull
    String formatResponseInfo(RequestInfo requestInfo);

    /**
     * 格式化输出的日志
     *
     * @param requestInfo
     */
    @NotNull
    String formatLogInfo(RequestInfo requestInfo);
}