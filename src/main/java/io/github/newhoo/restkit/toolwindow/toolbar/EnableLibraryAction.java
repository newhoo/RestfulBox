package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseToggleAction;
import io.github.newhoo.restkit.toolwindow.ToolWindowHelper;
import org.jetbrains.annotations.NotNull;

/**
 * EnableLibraryAction
 */
public class EnableLibraryAction extends BaseToggleAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.scanlibrary.action.text"));
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return false;
        }
        return ConfigHelper.getCommonSetting(project).isScanServiceWithLib();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        ConfigHelper.getCommonSetting(project).setScanServiceWithLib(state);

        ToolWindowHelper.scheduleUpdateTree(project);
    }
}
