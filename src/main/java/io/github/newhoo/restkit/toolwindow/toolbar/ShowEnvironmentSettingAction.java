package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.config.request.RequestConfigurable;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ShowEnvironmentSettingAction
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class ShowEnvironmentSettingAction extends BaseAnAction implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.environment.action.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String selectProject = Objects.toString(RestDataKey.SELECTED_PROJECT.getData(e.getDataContext()), e.getProject().getName());
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), RequestConfigurable.class, c -> c.selectProjectTab(selectProject, 1));
    }
}