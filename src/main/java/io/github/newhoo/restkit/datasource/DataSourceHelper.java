package io.github.newhoo.restkit.datasource;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.RestRegistry;
import io.github.newhoo.restkit.config.global.GlobalSettingConfigurable;
import io.github.newhoo.restkit.datasource.ep.DataSourceProvider;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static io.github.newhoo.restkit.common.RestConstant.DATA_SOURCE_IDE;

public class DataSourceHelper {

    public static DataSource getDataSource() {
        return DataSourceProvider.EP_NAME.getExtensionList()
                                         .stream()
                                         .filter(Objects::nonNull)
                                         .map(DataSourceProvider::createRepository)
                                         .filter(Objects::nonNull)
                                         .filter(repo -> repo.name().equals(DATA_SOURCE_IDE))
                                         .peek(repo -> {
                                             repo.init("");
                                         })
                                         .findFirst()
                                         .orElse(null);
    }

    public static void trySyncApiToDataSource(List<RestItem> itemList, Project project) {
        if (itemList == null || itemList.isEmpty()) {
            return;
        }
        DataSource dataSource = DataSourceHelper.getDataSource();
        if (DATA_SOURCE_IDE.equals(dataSource.name())) {
            NotifierUtils.infoBalloon("", RestBundle.message("toolkit.datasource.syncide.msg"), new NotificationAction(RestBundle.message("toolkit.common.btn.edit")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, GlobalSettingConfigurable.class);
                }
            }, project);
            return;
        }
    }

    public static void addRestItemToDataSource(List<RestItem> restItems) {
    }

    public static void updateRestItemToDataSource(List<RestItem> restItems) {
    }

    public static void syncCrossProjects(List<RestItem> restItems) {
    }

    public static void refreshCache(Project project) {
        DataSource dataSource = DataSourceHelper.getDataSource();
        dataSource.refreshCache();
        if (RestRegistry.debugMode()) {
            NotifierUtils.infoBalloon("", RestBundle.message("toolkit.datasource.cleancache.msg", dataSource.name()), null, project);
        }
    }
}
