package io.github.newhoo.restkit.restful;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.RestItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * rest item detail extension point
 *
 * @author huzunrong
 * @since 3.0.0
 */
@NotProguard
public interface RestItemDetail {

    /**
     * supported protocol
     */
    @NotNull
    String getProtocol();

    /**
     * 协议排序
     */
    default int getProtocolOrder() {
        return 999;
    }

    /**
     * tree node icon
     */
    @NotNull
    Icon getIcon(@NotNull RestItem restItem);

    /**
     * tree node name
     *
     * @param restItem
     * @param useApiDesc see in the config
     * @return not null or keep empty to use default logic
     */
    @NotNull
    default String getName(@NotNull RestItem restItem, boolean useApiDesc) {
        return "";
    }

    /**
     * DoubleClickOrEnter
     *
     * @param restItem
     * @return true - 不再执行原有方法  false - 继续执行原有逻辑
     */
    default boolean handleDoubleClickOrEnter(@NotNull RestItem restItem, @NotNull Project project) {
        return false;
    }
}