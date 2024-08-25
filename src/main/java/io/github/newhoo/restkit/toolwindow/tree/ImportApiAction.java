package io.github.newhoo.restkit.toolwindow.tree;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.global.GlobalSettingConfigurable;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.intellij.CompactHelper;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.toolwindow.ToolWindowHelper;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.DATA_SOURCE_IDE;

/**
 * ImportApiAction
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class ImportApiAction extends BaseAnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.common.btn.import"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        Transferable contents = CopyPasteManager.getInstance().getContents();
        if (contents == null) {
            return;
        }

        List<RestItem> restItems;
        try {
            String data = contents.getTransferData(DataFlavor.stringFlavor).toString();
            if (StringUtils.isEmpty(data) || !StringUtils.startsWith(data, "[") || !StringUtils.endsWith(data, "]")) {
                NotifierUtils.errorBalloon(RestBundle.message("toolkit.toolwindow.tree.importapi.title"), RestBundle.message("toolkit.toolwindow.tree.importapi.content", "format error"), project);
                return;
            }
            restItems = JsonUtils.fromJsonArr(data, RestItem.class);
            if (CollectionUtils.isEmpty(restItems) || restItems.stream().anyMatch(item -> StringUtils.isEmpty(item.getUrl()))) {
                NotifierUtils.errorBalloon(RestBundle.message("toolkit.toolwindow.tree.importapi.title"), RestBundle.message("toolkit.toolwindow.tree.importapi.content", "format error"), project);
                return;
            }
        } catch (Exception ex) {
            NotifierUtils.errorBalloon(RestBundle.message("toolkit.toolwindow.tree.importapi.title"), RestBundle.message("toolkit.toolwindow.tree.importapi.content", ex.toString()), project);
            return;
        }

        // 选择存储方式
        List<String> opList = new ArrayList<>();
        DataSource dataSource = DataSourceHelper.getDataSource();
        if (!DATA_SOURCE_IDE.equals(dataSource.name())) {
            opList.add(dataSource.name());
        }
        Map<String, RequestResolver> requestResolverMap = RequestHelper.getAllStorageRequestResolvers(project)
                                                                       .stream()
                                                                       .collect(Collectors.toMap(RequestResolver::getFrameworkName, o -> o, (o1, o2) -> o1));
        if (!requestResolverMap.isEmpty()) {
            requestResolverMap.keySet().forEach(s -> opList.add(s + "(Deprecated)"));
        }
        if (opList.isEmpty()) {
            NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.tree.importapi.ide.msg"), new NotificationAction(RestBundle.message("toolkit.common.btn.edit")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, GlobalSettingConfigurable.class);
                }
            }, project);
            return;
        }

        String[] ops = opList.toArray(new String[0]);
        int i = opList.size() == 1 ? 0 : CompactHelper.showChooseDialog("Select storage way", "Import Apis", ops, ops[0], null);
        if (i < 0) {
            return;
        }
        String selectOp = ops[i];
        RequestResolver useRequestResolver;
        if (StringUtils.contains(selectOp, "(Deprecated)")) {
            useRequestResolver = requestResolverMap.get(selectOp.replace("(Deprecated)", ""));
        } else {
            useRequestResolver = null;
        }

        // 输入项目名称
        String[] projects = new String[0];
        String projectName = CompactHelper.showEditableChooseDialog("Select or input project name", "Import Apis", null, projects, projects.length > 0 ? projects[0] : project.getName(), null);
        if (projectName == null) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "[RestfulBox] Import api to " + dataSource.name()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                restItems.forEach(o -> {
                    if (StringUtils.isEmpty(projectName)) {
                        o.setProject(StringUtils.defaultIfEmpty(o.getProject(), project.getName()) + "(import)");
                        o.setModuleName(Objects.toString(o.getModuleName(), "module-name"));
                    } else {
                        o.setProject(projectName);
                        o.setModuleName(Objects.toString(o.getModuleName(), "module-name") + "(import)");
                    }
                    if (useRequestResolver == null) {
                        // o.setFramework(dataSource.name());
                    } else {
                        o.setFramework(useRequestResolver.getFrameworkName());
                    }

                    o.setUrl(StringUtils.defaultString(o.getUrl()));
                    o.setBodyJson(StringUtils.defaultString(o.getBodyJson()));
                    o.setDescription(StringUtils.defaultString(o.getDescription()));
                    o.setTs(System.currentTimeMillis());
                    o.setPackageName(Objects.toString(o.getPackageName(), "package-name"));
                });
                if (useRequestResolver == null) {
                    DataSourceHelper.addRestItemToDataSource(restItems);
                } else {
                    useRequestResolver.add(restItems);
                    DataSourceHelper.syncCrossProjects(restItems);
                }
                ToolWindowHelper.scheduleUpdateTree(project);
            }
        });
    }
}
