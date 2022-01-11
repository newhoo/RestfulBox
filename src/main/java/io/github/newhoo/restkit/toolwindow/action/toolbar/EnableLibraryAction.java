package io.github.newhoo.restkit.toolwindow.action.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.toolwindow.RestServiceToolWindow;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.jetbrains.annotations.NotNull;

/**
 * EnableLibraryAction
 */
public class EnableLibraryAction extends ToggleAction {

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return false;
        }
        return CommonSettingComponent.getInstance(project).getState().isScanServiceWithLib();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        CommonSettingComponent.getInstance(project).getState().setScanServiceWithLib(state);

        RestToolWindowFactory.getRestServiceToolWindow(project, RestServiceToolWindow::scheduleUpdateTree);
    }
}
