package io.github.newhoo.restkit.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.newhoo.restkit.common.RestItem;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 本地API库
 *
 * @author huzunrong
 * @since 1.0.8
 */
@Data
@State(name = "RESTKit_LocalApiLibrary", storages = {@Storage("restkit/RESTKit_LocalApiLibrary.xml")})
public class LocalApiLibrary implements PersistentStateComponent<LocalApiLibrary> {

    private List<RestItem> itemList = new ArrayList<>();

    public static LocalApiLibrary getInstance(Project project) {
        return project.getService(LocalApiLibrary.class);
    }

    @Override
    public LocalApiLibrary getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull LocalApiLibrary state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
