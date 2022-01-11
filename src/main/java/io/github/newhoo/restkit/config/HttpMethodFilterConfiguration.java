package io.github.newhoo.restkit.config;

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.HttpMethod;

/**
 * HttpMethodFilterConfiguration
 */
@State(name = "HttpMethodFilterConfiguration", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class HttpMethodFilterConfiguration extends ChooseByNameFilterConfiguration<HttpMethod> {

    public static HttpMethodFilterConfiguration getInstance(Project project) {
        return project.getService(HttpMethodFilterConfiguration.class);
    }

    @Override
    protected String nameForElement(HttpMethod type) {
        return type.name();
    }
}
