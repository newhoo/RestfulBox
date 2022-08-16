package io.github.newhoo.restkit.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.i18n.RestBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * HttpSettingConfigurable
 *
 * @author huzunrong
 * @since 2.0.8
 */
public class HttpSettingConfigurable implements Configurable {

    private final HttpSetting httpSetting;
    private final HttpSettingForm httpSettingForm;

    private HttpSettingConfigurable(Project project) {
        this.httpSetting = HttpSettingComponent.getInstance(project).getState();
        this.httpSettingForm = new HttpSettingForm(project);
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "HTTP";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return httpSettingForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return httpSetting.isModified(httpSettingForm.getModifiedSetting());
    }

    @Override
    public void apply() {
        httpSettingForm.saveTo(httpSetting);
    }

    @Override
    public void reset() {
        httpSettingForm.reset(httpSetting);
    }
}