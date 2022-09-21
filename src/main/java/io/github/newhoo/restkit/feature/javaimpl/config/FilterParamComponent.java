package io.github.newhoo.restkit.feature.javaimpl.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 过滤参数
 *
 * @author huzunrong
 * @since 2.0.3
 */
@Data
@State(name = "RESTKit_FilterParameter", storages = {@Storage("restkit/RESTKit_FilterParameter.xml")})
public class FilterParamComponent implements PersistentStateComponent<FilterParamComponent> {

    private Set<String> qualifiedNames = new LinkedHashSet<>(Arrays.asList(
            "org.springframework.web.bind.annotation.RequestHeader",
            "org.springframework.web.bind.annotation.RequestBody",
            "javax.servlet.http.HttpServletRequest",
            "javax.servlet.http.HttpServletResponse",
            "org.springframework.web.context.request.ServletWebRequest"
    ));

    public static FilterParamComponent getInstance(Project project) {
        return ServiceManager.getService(project, FilterParamComponent.class);
    }

    @Override
    public FilterParamComponent getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull FilterParamComponent state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
