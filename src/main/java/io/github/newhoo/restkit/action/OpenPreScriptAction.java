package io.github.newhoo.restkit.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.SettingConfigurable;
import io.github.newhoo.restkit.util.HtmlUtil;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.nio.file.Paths;

/**
 * open pre-request script in editor
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class OpenPreScriptAction extends DumbAwareAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        CommonSetting setting = CommonSettingComponent.getInstance(project).getState();
        String requestScriptPath = isPreScript() ? setting.getPreRequestScriptPath() : setting.getPostRequestScriptPath();
        e.getPresentation().setEnabled(StringUtils.isNotEmpty(requestScriptPath));
    }

    protected boolean isPreScript() {
        return true;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        CommonSetting setting = CommonSettingComponent.getInstance(project).getState();
        String requestScriptPath = isPreScript() ? setting.getPreRequestScriptPath() : setting.getPostRequestScriptPath();
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(Paths.get(requestScriptPath));
        if (file != null) {
            VfsUtil.markDirtyAndRefresh(true, false, false, file);
            FileEditorManager.getInstance(project).openFile(file, true);
        } else {
            NotifierUtils.infoBalloon("Cannot find request script path", requestScriptPath + " " + HtmlUtil.link("Edit", "Edit"), new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingConfigurable.class);
                }
            }, project);
//            String title = "Cannot find '" + requestScriptPath + "'";
//            Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, title, "", NotificationType.INFORMATION), project);
        }
    }
}
