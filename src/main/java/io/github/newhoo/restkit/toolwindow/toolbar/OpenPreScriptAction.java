package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.config.request.RequestConfigurable;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * open pre-request script in editor
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class OpenPreScriptAction extends BaseAnAction implements DumbAware {

    protected boolean isPreScript() {
        return true;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.prescript.action.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        String selectProject = Objects.toString(RestDataKey.SELECTED_PROJECT.getData(e.getDataContext()), project.getName());

        RequestSetting setting = DataSourceHelper.getDataSource().selectRequestSetting(selectProject, project);
        String requestScriptPath = isPreScript() ? setting.getPreRequestScriptPath() : setting.getPostRequestScriptPath();
        if (StringUtils.isEmpty(requestScriptPath)) {
            NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.toolbar.script.msg", selectProject), new NotificationAction(RestBundle.message("toolkit.common.btn.edit")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, RequestConfigurable.class, c -> c.selectProjectTab(selectProject, 0));
                }
            }, project);
            return;
        }
        requestScriptPath = FileUtils.expandUserHome(requestScriptPath);
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(Paths.get(requestScriptPath));
        if (file != null) {
            VfsUtil.markDirtyAndRefresh(true, false, false, file);
            FileEditorManager.getInstance(project).openFile(file, true);
        } else {
            NotifierUtils.infoBalloon("", RestBundle.message("toolkit.toolwindow.toolbar.script.msg", selectProject) + requestScriptPath, new NotificationAction(RestBundle.message("toolkit.common.btn.edit")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, RequestConfigurable.class, c -> c.selectProjectTab(selectProject, 0));
                }
            }, project);
        }
    }
}
