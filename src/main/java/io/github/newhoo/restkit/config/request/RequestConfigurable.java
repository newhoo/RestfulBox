package io.github.newhoo.restkit.config.request;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.config.CommonHeader;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static io.github.newhoo.restkit.config.SettingListener.ENV_UPDATE;

/**
 * RequestConfigurable
 *
 * @author huzunrong
 * @since 1.0
 */
public class RequestConfigurable implements Configurable {

    private final Project project;
    private final RequestSettingForm requestSettingForm;

    private final DataSource repository = DataSourceHelper.getDataSource();
    private RequestSetting cacheSetting = null;
    private Environment cacheEnvironment = null;
    private CommonHeader cacheHeader = null;

    public RequestConfigurable(Project project) {
        this.project = project;
        this.requestSettingForm = new RequestSettingForm(project, selectProject -> {
            cacheSetting = repository.selectRequestSetting(selectProject, project);
            cacheEnvironment = repository.selectEnvironment(selectProject, project);
            cacheHeader = repository.selectHeader(selectProject, project);
            reset();
        });
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Request Setting";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return requestSettingForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return requestSettingForm.isModified(cacheSetting)
                || requestSettingForm.isModified(cacheEnvironment)
                || requestSettingForm.isModified(cacheHeader);
    }

    @Override
    public void apply() throws ConfigurationException {
        requestSettingForm.checkInput();
        if (requestSettingForm.isModified(cacheSetting)) {
            RequestSetting modifiedSetting = requestSettingForm.getModifiedSetting();
            modifiedSetting.setPreRequestScriptContent(cacheSetting.getPreRequestScriptContent());
            modifiedSetting.setPostRequestScriptContent(cacheSetting.getPostRequestScriptContent());
            cacheSetting = modifiedSetting;
            repository.syncRequestSetting(cacheSetting, project);
        }

        if (requestSettingForm.isModified(cacheEnvironment)) {
            cacheEnvironment = requestSettingForm.getModifiedEnvironment();
            repository.syncEnvironment(cacheEnvironment, project);

            project.getMessageBus().syncPublisher(ENV_UPDATE).changeEnv(cacheEnvironment.getProject());
        }

        if (requestSettingForm.isModified(cacheHeader)) {
            cacheHeader = requestSettingForm.getModifiedHeader();
            repository.syncHeader(cacheHeader, project);
        }
    }

    @Override
    public void reset() {
        if (cacheSetting == null) {
            cacheSetting = repository.selectRequestSetting(project.getName(), project);
        }
        if (cacheEnvironment == null) {
            cacheEnvironment = repository.selectEnvironment(project.getName(), project);
        }
        if (cacheHeader == null) {
            cacheHeader = repository.selectHeader(project.getName(), project);
        }
        requestSettingForm.reset(cacheSetting.getProject());
        requestSettingForm.reset(cacheSetting);
        requestSettingForm.reset(cacheEnvironment);
        requestSettingForm.reset(cacheHeader);
    }

    public void selectProjectTab(String project, int tabIndex) {
        requestSettingForm.selectProjectTab(project, tabIndex);
    }
}