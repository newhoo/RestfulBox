package io.github.newhoo.restkit.restful;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.RequestInfo;
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
     * 发送请求
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