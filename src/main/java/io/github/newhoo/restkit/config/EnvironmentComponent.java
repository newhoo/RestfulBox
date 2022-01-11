package io.github.newhoo.restkit.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * EnvironmentComponent
 *
 * @author huzunrong
 */
@State(name = "RESTKit_Environment", storages = {@Storage("restkit/RESTKit_Environment.xml")})
public class EnvironmentComponent implements PersistentStateComponent<Environment> {

    private final Environment environment = new Environment();

    @NotNull
    @Override
    public Environment getState() {
        return environment;
    }

    @Override
    public void loadState(@NotNull Environment state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
