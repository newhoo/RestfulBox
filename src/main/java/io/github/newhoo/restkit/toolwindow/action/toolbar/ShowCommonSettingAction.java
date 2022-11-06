package io.github.newhoo.restkit.toolwindow.action.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import io.github.newhoo.restkit.config.SettingConfigurable;
import org.jetbrains.annotations.NotNull;

/**
 * ShowCommonSettingAction
 *
 * @author huzunrong
 * @since 1.0.0
 */
public class ShowCommonSettingAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), SettingConfigurable.class);
    }
}