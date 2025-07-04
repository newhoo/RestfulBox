package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import io.github.newhoo.restkit.config.request.RequestConfigurable;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import org.jetbrains.annotations.NotNull;

/**
 * ShowRequestSettingAction
 *
 * @author newhoo
 * @since 3.0.3
 */
public class ShowRequestSettingAction extends BaseAnAction implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.request.action.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), RequestConfigurable.class);
    }
}