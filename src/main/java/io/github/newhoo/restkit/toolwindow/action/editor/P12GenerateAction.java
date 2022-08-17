package io.github.newhoo.restkit.toolwindow.action.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.util.ToolkitUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_BASE_URL;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_PASSWD;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_PATH;
import static io.github.newhoo.restkit.common.RestConstant.PROTOCOL;
import static io.github.newhoo.restkit.common.RestConstant.PROTOCOL_HTTP;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_URL_HTTP;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_URL_HTTPS;

/**
 * P12GenerateAction
 *
 * @author huzunrong
 * @since 2.0.7
 */
public class P12GenerateAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        RestClientApiInfo apiInfo = RestDataKey.CLIENT_API_INFO.getData(e.getDataContext());
        if (project == null || apiInfo == null) {
            e.getPresentation().setVisible(false);
            return;
        }
        // http请求
        if (StringUtils.startsWith(apiInfo.getUrl(), HTTP_URL_HTTP)) {
            e.getPresentation().setVisible(false);
            return;
        }
        Map<String, String> configMap = ToolkitUtil.textToModifiableMap(apiInfo.getConfig());
        // 已包含
        if (configMap.containsKey(HTTP_P12_PATH) && configMap.containsKey(HTTP_P12_PASSWD)) {
            e.getPresentation().setVisible(false);
            return;
        }
        // 非http协议
        if (configMap.containsKey(PROTOCOL) && !PROTOCOL_HTTP.equals(configMap.get(PROTOCOL))) {
            e.getPresentation().setVisible(false);
            return;
        }
        // 非https请求
        if (!StringUtils.startsWith(apiInfo.getUrl(), HTTP_URL_HTTPS)) {
            String baseUrl = configMap.get(HTTP_BASE_URL);
            if (StringUtils.isEmpty(baseUrl)) {
                e.getPresentation().setVisible(false);
                return;
            }
            if (!StringUtils.startsWith(baseUrl, "{{")
                    && !StringUtils.startsWith(baseUrl, HTTP_URL_HTTPS)) {
                e.getPresentation().setVisible(false);
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        RestClientApiInfo apiInfo = RestDataKey.CLIENT_API_INFO.getData(e.getDataContext());
        if (project == null || apiInfo == null) {
            return;
        }
        Document doc = e.getRequiredData(CommonDataKeys.EDITOR).getDocument();

        String newConfigText = doc.getText() + "\np12Path: {{p12Path}}\np12Passwd: {{p12Passwd}}";
        WriteCommandAction.runWriteCommandAction(project, () -> {
            doc.setText(newConfigText);
        });
    }
}
