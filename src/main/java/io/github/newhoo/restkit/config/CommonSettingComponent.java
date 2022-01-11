package io.github.newhoo.restkit.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * CommonSettingComponent
 *
 * @author huzunrong
 * @since 2.0.0
 */
@State(name = "RESTKit_Setting", storages = {@Storage("restkit/RESTKit_Setting.xml")})
public class CommonSettingComponent implements PersistentStateComponent<CommonSetting> {

    private final CommonSetting commonSetting = new CommonSetting();

    public static CommonSettingComponent getInstance(Project project) {
        return project.getService(CommonSettingComponent.class);
    }

    @NotNull
    @Override
    public CommonSetting getState() {
        return commonSetting;
    }

    @Override
    public void loadState(@NotNull CommonSetting state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
