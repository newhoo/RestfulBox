package io.github.newhoo.restkit.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * HttpSettingComponent
 *
 * @author huzunrong
 * @since 2.0.8
 */
@State(name = "RESTKit_HttpSetting", storages = {@Storage("restkit/RESTKit_HttpSetting.xml")})
public class HttpSettingComponent implements PersistentStateComponent<HttpSetting> {

    private final HttpSetting httpSetting = new HttpSetting();

    public static HttpSettingComponent getInstance(Project project) {
        return project.getService(HttpSettingComponent.class);
    }

    @NotNull
    @Override
    public HttpSetting getState() {
        return httpSetting;
    }

    @Override
    public void loadState(@NotNull HttpSetting state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
