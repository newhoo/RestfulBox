package io.github.newhoo.restkit.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.util.EnvironmentUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * EnvironmentConfigurable
 *
 * @author huzunrong
 * @since 1.0
 */
public class EnvironmentConfigurable implements Configurable {

    private final Project project;
    private final Environment environment;
    private final EnvironmentForm environmentForm;

    private EnvironmentConfigurable(Project project) {
        this.project = project;
        this.environment = Environment.getInstance(project);
        this.environmentForm = project.getComponent(EnvironmentForm.class);
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return RestBundle.message("toolkit.environment.name");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return environmentForm.mainPanel;
    }

    @Override
    public boolean isModified() {
        return environmentForm.isModified(environment);
    }

    @Override
    public void apply() {
        environmentForm.saveTo(environment);
        EnvironmentUtils.notifyEnvUpdate(project);
    }

    @Override
    public void reset() {
        environmentForm.reset(environment);
    }
}