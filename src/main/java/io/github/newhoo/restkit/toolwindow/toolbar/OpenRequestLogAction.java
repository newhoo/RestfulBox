package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;

/**
 * open request log in editor
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class OpenRequestLogAction extends BaseAnAction implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.log.action.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        String todayLog = FileUtils.getTodayLog();
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(Paths.get(todayLog));
        if (file != null) {
            VfsUtil.markDirtyAndRefresh(true, false, false, file);
            FileEditorManager.getInstance(project).openFile(file, true);
        } else {
            String logDirectory = FileUtils.getLogDirectory();
            NotifierUtils.infoBalloon(RestBundle.message("toolkit.toolwindow.toolbar.log.tip.title"), logDirectory, new NotificationAction(RestBundle.message("toolkit.toolwindow.toolbar.log.tip.open")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    RevealFileAction.openFile(new File(logDirectory));
                }
            }, project);
        }
    }
}
