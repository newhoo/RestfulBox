package io.github.newhoo.restkit.toolwindow.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.github.newhoo.restkit.common.RestConstant.PLACEHOLDER_BASE_URL;
import static io.github.newhoo.restkit.common.RestConstant.PROTOCOL;
import static io.github.newhoo.restkit.common.RestConstant.PROTOCOL_HTTP;

/**
 * CopyCurlAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class CopyCurlAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        RestClientApiInfo apiInfo = RestDataKey.CLIENT_API_INFO.getData(e.getDataContext());
        if (project == null || apiInfo == null) {
            e.getPresentation().setVisible(false);
            return;
        }
        Map<String, String> configMap = ToolkitUtil.textToModifiableMap(apiInfo.getConfig());
        if (configMap.containsKey(PROTOCOL) && !PROTOCOL_HTTP.equals(configMap.get(PROTOCOL))) {
            e.getPresentation().setVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        RestClientApiInfo apiInfo = RestDataKey.CLIENT_API_INFO.getData(e.getDataContext());
        if (project == null || apiInfo == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("curl ");

        // header
        Map<String, String> headerMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(apiInfo.getHeaders(), project));

        headerMap.forEach((k, v) -> {
            sb.append("-H").append(" ").append("\"").append(k).append(": ").append(v).append("\"").append(" ");
        });

        String bodyJson = apiInfo.getBodyJson();
        if (StringUtils.isNotEmpty(bodyJson)) {
            sb.append("-H \"Content-Type: application/json;charset=UTF-8\" ");
            sb.append("-d '").append(bodyJson).append("'").append(" ");
        }

        sb.append("-X").append(" ").append(apiInfo.getMethod().name()).append(" ");

        String url = apiInfo.getUrl();
        if (!url.contains("://")) {
            url = EnvironmentUtils.handlePlaceholderVariable(PLACEHOLDER_BASE_URL + url, project);
            // 环境变量未设置【baseUrl】时强行替换为localhost:8080
            url = url.replaceFirst("\\{\\{baseUrl}}", "http://localhost:8080");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        // param
        Map<String, String> paramMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(apiInfo.getParams(), project));
        if (!paramMap.isEmpty()) {
            // 替换URL 路径参数
            for (String key : paramMap.keySet()) {
                url = url.replaceFirst("\\{(" + key + "[\\s\\S]*?)}", StringUtils.defaultString(paramMap.get(key)));
                sb.append("-d '").append(key).append("=").append(paramMap.get(key)).append("'").append(" ");
            }
//            String params = ToolkitUtil.getRequestParam(paramMap);
//            // URL可能包含了参数
//            url += url.contains("?") ? "&" + params : "?" + params;
        }
        sb.append(url);
        if (url.startsWith("https://")) {
            sb.append(" -k");

            Map<String, String> configMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(apiInfo.getConfig(), project));
            String p12Path = configMap.get("p12Path");
            String p12Passwd = configMap.get("p12Passwd");
            // 双向认证
            if (!StringUtils.isAnyEmpty(p12Path, p12Passwd)) {
                sb.append(" --cert-type P12 --cert ").append(p12Path).append(":").append(p12Passwd);
            }
        }

        IdeaUtils.copyToClipboard(sb.toString());
        NotifierUtils.infoBalloon("", "Curl copied to clipboard successfully.", null, project);
    }
}
