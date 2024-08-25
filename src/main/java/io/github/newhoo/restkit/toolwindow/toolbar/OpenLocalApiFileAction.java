package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.newhoo.restkit.common.RestRegistry;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.config.ide.CommonSetting;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

/**
 * open local api store file in editor
 *
 * @author huzunrong
 * @since 2.0.2
 */
public class OpenLocalApiFileAction extends BaseAnAction implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        if (RestRegistry.enableLocalApi()) {
            e.getPresentation().setVisible(true);
            e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.localapi.action.text"));
        } else {
            e.getPresentation().setVisible(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        CommonSetting setting = ConfigHelper.getCommonSetting(project);
        String path = StringUtils.defaultIfEmpty(setting.getApiFilePath(), FileUtils.getApiFilePath(project));
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(Paths.get(path));
        if (file != null) {
            VfsUtil.markDirtyAndRefresh(true, false, false, file);
            FileEditorManager.getInstance(project).openFile(file, true);
        } else {
            NotifierUtils.infoBalloon(RestBundle.message("toolkit.toolwindow.toolbar.localapi.msg"), FileUtils.getRestDirectory(project), null, project);
        }
    }
}
