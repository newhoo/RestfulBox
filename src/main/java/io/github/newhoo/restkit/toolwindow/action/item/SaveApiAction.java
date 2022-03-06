package io.github.newhoo.restkit.toolwindow.action.item;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.config.SettingConfigurable;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.util.HtmlUtil;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SaveApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class SaveApiAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        RestClientApiInfo apiInfo = RestDataKey.CLIENT_API_INFO.getData(e.getDataContext());
        if (apiInfo == null) {
            return;
        }
        String url = apiInfo.getUrl();
        if (StringUtils.isEmpty(url)) {
            NotifierUtils.errorBalloon("Save Api Error", "Url can't be empty.", project);
            return;
        }

        Map<String, RequestResolver> resolverMap = RequestHelper.getEnabledRequestResolvers(project)
                                                                .stream()
                                                                .filter(o -> o.getScanType() == RequestResolver.ScanType.STORAGE)
                                                                .collect(Collectors.toMap(RequestResolver::getFrameworkName, o -> o, (o1, o2) -> o1));
        if (resolverMap.isEmpty()) {
            NotifierUtils.infoBalloon("", "Supported store frameworks disabled. " + HtmlUtil.link("Edit", "Edit"), new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingConfigurable.class);
                }
            }, project);
            return;
        }

        new SaveApiDialog(project, resolverMap, apiInfo).setVisible(true);
    }
}
