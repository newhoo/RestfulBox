package io.github.newhoo.restkit.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.newhoo.restkit.common.KV;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 参数库
 *
 * @author huzunrong
 * @since 1.0.8
 */
@Data
@State(name = "RESTKit_ParameterLibrary", storages = {@Storage("restkit/RESTKit_ParameterLibrary.xml")})
public class ParameterLibrary implements PersistentStateComponent<ParameterLibrary> {

    private Map<String, Map<String, List<KV>>> parameterMap = new HashMap<>(0);

    public static ParameterLibrary getInstance(Project project) {
        return project.getService(ParameterLibrary.class);
    }

    @Override
    public ParameterLibrary getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ParameterLibrary state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
