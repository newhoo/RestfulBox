package io.github.newhoo.restkit.toolwindow.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static io.github.newhoo.restkit.common.RestConstant.PLACEHOLDER_BASE_URL;

/**
 * CopyCurlAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class CopyCurlAction extends AnAction {

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
            Environment environment = Environment.getInstance(project);
            Map<String, String> env = environment.getCurrentEnabledEnvMap();

            // TODO huzunrong, 2022/8/2 23:28 []
//            EnvironmentUtils.handlePlaceholderVariable()
            sb.append(" -k");
            sb.append(" --cert-type P12 --cert ")
              .append(env.getOrDefault("p12Path", "p12Path"))
              .append(":")
              .append(env.getOrDefault("p12Passwd", "p12Passwd"));
        }

        IdeaUtils.copyToClipboard(sb.toString());
        NotifierUtils.infoBalloon("", "Curl copied to clipboard successfully.", null, e.getRequiredData(CommonDataKeys.PROJECT));
    }
}
