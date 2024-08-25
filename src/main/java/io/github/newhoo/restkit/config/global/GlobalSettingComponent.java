package io.github.newhoo.restkit.config.global;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.newhoo.restkit.common.NotProguard;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * GlobalSettingComponent
 *
 * @author huzunrong
 * @since 2.1.2
 */
@State(name = "RESTKit_GlobalSetting", storages = {@Storage("RESTKit_GlobalSetting.xml")})
public class GlobalSettingComponent implements PersistentStateComponent<GlobalSetting> {

    @NotProguard
    private final GlobalSetting globalSetting = new GlobalSetting();

    public static GlobalSettingComponent getInstance() {
        return ApplicationManager.getApplication().getService(GlobalSettingComponent.class);
    }

    @NotNull
    @Override
    public GlobalSetting getState() {
        return this.globalSetting;
    }

    @Override
    public void loadState(@NotNull GlobalSetting state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
