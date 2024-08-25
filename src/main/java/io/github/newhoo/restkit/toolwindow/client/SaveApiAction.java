package io.github.newhoo.restkit.toolwindow.client;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestClientApiInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.config.global.GlobalSettingConfigurable;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.RestClient;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.DATA_SOURCE_IDE;

/**
 * SaveApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class SaveApiAction extends BaseAnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.client.saveapi.action.text"));
    }

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
            NotifierUtils.errorBalloon(RestBundle.message("toolkit.toolwindow.client.saveapi.error.title"), RestBundle.message("toolkit.toolwindow.client.saveapi.error.msg"), project);
            return;
        }

        Map<String, RequestResolver> requestResolverMap = RequestHelper.getAllStorageRequestResolvers(project)
                                                                       .stream()
                                                                       .collect(Collectors.toMap(RequestResolver::getFrameworkName, o -> o, (o1, o2) -> o1));

        DataSource dataSource = DataSourceHelper.getDataSource();
        if (DATA_SOURCE_IDE.equals(dataSource.name()) && requestResolverMap.isEmpty()) {
            NotifierUtils.infoBalloon("", RestBundle.message("toolkit.datasource.syncide.msg"), new NotificationAction(RestBundle.message("toolkit.common.btn.edit")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, GlobalSettingConfigurable.class);
                }
            }, project);
            return;
        }

        List<String> protocols = RequestHelper.getRestClient()
                                              .stream()
                                              .map(RestClient::getProtocol)
                                              .collect(Collectors.toList());

        new SaveApiDialog(project, apiInfo, dataSource, protocols, requestResolverMap).show();
    }
}
