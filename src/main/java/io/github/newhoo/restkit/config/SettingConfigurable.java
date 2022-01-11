package io.github.newhoo.restkit.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.i18n.RestBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * SettingConfigurable
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class SettingConfigurable implements Configurable {

    private final CommonSetting commonSetting;
    private final SettingForm settingForm;

    private SettingConfigurable(Project project) {
        this.commonSetting = CommonSettingComponent.getInstance(project).getState();
        this.settingForm = project.getComponent(SettingForm.class);
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return RestBundle.message("toolkit.config.name");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return settingForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return commonSetting.isModified(settingForm.getModifiedSetting());
    }

    @Override
    public void apply() {
        settingForm.saveTo(commonSetting);
    }

    @Override
    public void reset() {
        settingForm.reset(commonSetting);
    }
}