package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.jetbrains.annotations.NotNull;

/**
 * ExpandAllAction
 */
public class ExpandAllAction extends BaseAnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.expandall.action.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        RestToolWindowFactory.getRestServiceToolWindow(project, toolWindow -> {
            toolWindow.expandAll(true);
        });
    }
}
