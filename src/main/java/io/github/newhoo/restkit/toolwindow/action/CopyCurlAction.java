package io.github.newhoo.restkit.toolwindow.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.SystemInfo;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import io.github.newhoo.restkit.util.IdeaUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_FILE_PREFIX;
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
        HttpMethod httpMethod = apiInfo.getMethod();
        if (httpMethod == null || httpMethod == HttpMethod.UNDEFINED) {
            return;
        }

        // header
        Map<String, String> headerMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(apiInfo.getHeaders(), project));
        if (HttpMethod.GET != httpMethod && StringUtils.isNotEmpty(apiInfo.getBodyJson())) {
            headerMap.putIfAbsent("Content-Type", "application/json;charset=UTF-8");
        }

        String url = apiInfo.getUrl();
        if (!url.contains("://")) {
            url = EnvironmentUtils.handlePlaceholderVariable(PLACEHOLDER_BASE_URL + url, project);
            // 环境变量未设置【baseUrl】时强行替换为localhost:8080
            url = url.replaceFirst("\\{\\{baseUrl}}", "http://localhost:8080");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        // 自带的query参数编码: todo curl要转义特殊字符
        if (url.contains("?")) {
            String[] split = StringUtils.split(url, "?", 2);
            url = split[0] + "?" + ToolkitUtil.encodeQueryParam(split[1]);
        }

        Map<String, String> paramMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(apiInfo.getParams(), project));
        // file params
        Map<String, String> fileParamsMap = paramMap.entrySet()
                                                    .stream()
                                                    .filter(entry -> StringUtils.startsWith(entry.getValue(), HTTP_FILE_PREFIX))
                                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // path variables
        Set<String> pathVariables = new HashSet<>(4);
        // 替换URL
        if (url.contains("{") && url.contains("}") && !paramMap.isEmpty()) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                if (StringUtils.isNotEmpty(entry.getValue()) && !fileParamsMap.containsKey(entry.getKey()) && url.contains(placeholder)) {
                    pathVariables.add(entry.getKey());
                    url = url.replace(placeholder, entry.getValue());
//                    url = url.replaceFirst("\\{(" + key + "[\\s\\S]*?)}", v);
                }
            }
        }
        // query/form params
        Map<String, String> queryOrFormParamsMap = paramMap.entrySet()
                                                           .stream()
                                                           .filter(entry -> !fileParamsMap.containsKey(entry.getKey()))
                                                           .filter(entry -> !pathVariables.contains(entry.getKey()))
                                                           .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // query params
        if (HttpMethod.GET == httpMethod || StringUtils.isNotEmpty(apiInfo.getBodyJson())) {
            fileParamsMap.clear();
            if (!queryOrFormParamsMap.isEmpty()) {
                String params = ToolkitUtil.getRequestParam(queryOrFormParamsMap);
                // URL可能包含了参数: todo curl要转义特殊字符
                url += url.contains("?") ? "&" + params : "?" + params;
            }
        }

        String p12Path = null, p12Passwd = null;
        if (url.startsWith("https://")) {
            Map<String, String> configMap = ToolkitUtil.textToModifiableMap(EnvironmentUtils.handlePlaceholderVariable(apiInfo.getConfig(), project));
            p12Path = configMap.get("p12Path");
            p12Passwd = configMap.get("p12Passwd");
        }


        // not windows
        if (!SystemInfo.isWindows) {
            doCopyCurl(url, httpMethod, headerMap,
                    fileParamsMap, queryOrFormParamsMap, apiInfo.getBodyJson(),
                    p12Path, p12Passwd, false, project);
            return;
        }

        String finalUrl = url;
        String finalP12Path = p12Path;
        String finalP12Passwd = p12Passwd;
        List<AnAction> actions = new ArrayList<>(4);
        actions.add(new AnAction("Copy") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                doCopyCurl(finalUrl, httpMethod, headerMap,
                        fileParamsMap, queryOrFormParamsMap, apiInfo.getBodyJson(),
                        finalP12Path, finalP12Passwd, false, project);
            }
        });
        if (!StringUtils.isAnyEmpty(p12Path, p12Passwd)
                || (HttpMethod.GET != httpMethod && StringUtils.isEmpty(apiInfo.getBodyJson()) && !fileParamsMap.isEmpty())) {
            actions.add(new AnAction("Copy for Wsl") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    doCopyCurl(finalUrl, httpMethod, headerMap,
                            fileParamsMap, queryOrFormParamsMap, apiInfo.getBodyJson(),
                            finalP12Path, finalP12Passwd, true, project);
                }
            });
        }

        if (actions.size() == 1) {
            actions.get(0).actionPerformed(e);
            return;
        }

        final ListPopup popup = JBPopupFactory.getInstance()
                                              .createActionGroupPopup(
                                                      null,
                                                      new DefaultActionGroup(actions),
                                                      e.getDataContext(),
                                                      JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                                      true);
        popup.showInBestPositionFor(e.getDataContext());
    }

    private void doCopyCurl(String url,
                            HttpMethod httpMethod,
                            Map<String, String> headerMap,
                            Map<String, String> fileParamsMap,
                            Map<String, String> queryOrFormParamsMap,
                            String bodyJson,
                            String p12Path, String p12Passwd,
                            boolean wsl,
                            Project project
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("curl ");
        sb.append("-X").append(" ").append(httpMethod.name()).append(" ");
        sb.append(url).append(" ");

        headerMap.forEach((k, v) -> {
            sb.append("-H").append(" ").append("'").append(k).append(": ").append(v).append("'").append(" ");
        });

        if (HttpMethod.GET != httpMethod) {
            if (StringUtils.isNotEmpty(bodyJson)) {
                sb.append("-d '").append(bodyJson).append("'").append(" ");
            } else {
                // form params: Content-Type: application/x-www-form-urlencoded
                if (fileParamsMap.isEmpty()) {
                    queryOrFormParamsMap.forEach((k, v) -> {
                        sb.append("-d '").append(k).append("=").append(v).append("'").append(" ");
                    });
                } else {
                    // form params: Content-Type: multipart/form-data; boundary=------------------------75a1b524af201d5c
                    queryOrFormParamsMap.forEach((k, v) -> {
                        sb.append("-F '").append(k).append("=").append(v).append("'").append(" ");
                    });
                    fileParamsMap.forEach((k, v) -> {
                        sb.append("-F '").append(k).append("=@\"").append(getFilepath(ToolkitUtil.getUploadFilepath(v), wsl)).append("\"'").append(" ");
                    });
                }
            }
        }

        if (url.startsWith("https://")) {
            sb.append("-k ");

            // 双向认证
            if (!StringUtils.isAnyEmpty(p12Path, p12Passwd)) {
                sb.append("--cert-type P12 --cert ").append("'").append(getFilepath(p12Path, wsl)).append("'").append(":").append("'").append(p12Passwd).append("'");
            }
        }

        IdeaUtils.copyToClipboard(sb.toString());
        NotifierUtils.infoBalloon("", "Curl copied to clipboard successfully.", null, project);
    }

    private String getFilepath(String filepath, boolean wsl) {
        if (wsl && StringUtils.contains(filepath, ':')) {
            filepath = filepath.substring(0, 1).toLowerCase() + filepath.substring(1);
            filepath = "/mnt/" + filepath.replace(":", "").replace('\\', '/');
        }
        return filepath;
    }
}
