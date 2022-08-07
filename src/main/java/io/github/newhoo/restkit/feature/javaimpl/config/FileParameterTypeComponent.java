package io.github.newhoo.restkit.feature.javaimpl.config;

import com.intellij.openapi.components.PersistentStateComponent;
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
 * 支持的文件参数类型
 *
 * @author huzunrong
 * @since 2.0.8
 */
@Data
@State(name = "RESTKit_FileParameterType", storages = {@Storage("restkit/RESTKit_FileParameterType.xml")})
public class FileParameterTypeComponent implements PersistentStateComponent<FileParameterTypeComponent> {

    private Set<String> qualifiedNames = new LinkedHashSet<>(Arrays.asList(
            "org.springframework.web.multipart.MultipartFile"
    ));

    public static FileParameterTypeComponent getInstance(Project project) {
        return project.getService(FileParameterTypeComponent.class);
    }

    @Override
    public FileParameterTypeComponent getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull FileParameterTypeComponent state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
