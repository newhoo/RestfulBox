package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class RefreshProjectAction extends BaseAnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> {
            return RestBundle.message("toolkit.toolwindow.toolbar.refresh.action.text");
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        DataSourceHelper.refreshCache(project);
        RestToolWindowFactory.getRestServiceToolWindow(project, restServiceToolWindow -> {
            restServiceToolWindow.scheduleUpdateTree(null, false);
        });
    }
}
