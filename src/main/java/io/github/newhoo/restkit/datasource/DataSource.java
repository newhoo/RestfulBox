package io.github.newhoo.restkit.datasource;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.ProjectInfo;
import io.github.newhoo.restkit.common.RestClientEditorInfo;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.CommonHeader;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.config.ProjectTypeSetting;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.config.certificate.Certificate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * datasource for plugin
 *
 * @author huzunrong
 * @since 3.0.0
 */
@NotProguard
public interface DataSource {

    /**
     * concise name about this datasource
     */
    String name();

    /**
     * datasource config template
     */
    @NotNull
    String getDefaultConfig();

    /**
     * check user's config
     *
     * @param config string format like #getDefaultConfig()
     * @return true/false
     */
    boolean checkConfig(String config);

    /**
     * init this datasource
     *
     * @param config checked config
     */
    void init(String config);

    /**
     * datasource status
     */
    boolean getStatus();

    /**
     * destroy datasource
     */
    void destroy();

    // ########################################## business operation #############################################

    default void refreshCache() {
    }

    @NotNull
    List<ProjectInfo> selectProjects(Project project);

    void renameProject(String projectName, String newProjectName, Project project);

    void removeProject(String projectName, Project project);

    @NotNull
    RequestSetting selectRequestSetting(String projectName, Project project);

    void syncRequestSetting(RequestSetting requestSetting, Project project);

    @NotNull
    String selectSetting(String projectName, String type, Project project);

    @NotNull
    List<ProjectTypeSetting> selectSetting(List<String> projectNames, List<String> types, Project project);

    void syncSetting(String projectName, String type, String content, Project project);

    @NotNull
    Environment selectEnvironment(String projectName, Project project);

    void syncEnvironment(Environment environment, Project project);

    @NotNull
    CommonHeader selectHeader(String projectName, Project project);

    void syncHeader(CommonHeader commonHeader, Project project);

    @NotNull
    List<Certificate> selectCertificate(Project project);

    @Nullable
    Certificate selectEnabledCertificate(String host, Project project);

    void syncCertificate(List<Certificate> certificates, Project project);

    @NotNull
    List<KV> selectParameter(RestClientEditorInfo editorInfo, Project project);

    void syncParameter(RestClientEditorInfo editorInfo, List<KV> parameterList, Project project);

    @NotNull
    List<RestItem> selectRestItems(@NotNull List<String> selectedProjectNames, Project project);

    void syncRestItems(List<RestItem> restItems, Project project);

    /**
     * add api
     *
     * @param restItems not null
     */
    void addRestItem(List<RestItem> restItems, Project project);

    /**
     * update api
     *
     * @param restItems not null
     */
    void updateRestItem(List<RestItem> restItems, Project project);

    /**
     * delete api
     *
     * @param restItems not null
     */
    void deleteRestItem(List<RestItem> restItems, Project project);
}
