package io.github.newhoo.restkit.toolwindow.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import io.github.newhoo.restkit.config.global.GlobalSettingConfigurable;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import org.jetbrains.annotations.NotNull;

/**
 * ShowGlobalSettingAction
 *
 * @author huzunrong
 * @since 1.0.0
 */
public class ShowGlobalSettingAction extends BaseAnAction implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(() -> RestBundle.message("toolkit.toolwindow.toolbar.global.action.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), GlobalSettingConfigurable.class);
    }
}