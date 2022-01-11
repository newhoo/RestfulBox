package io.github.newhoo.restkit.restful;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * resolve restful apis in project
 *
 * @author huzunrong
 * @since 2.0.1
 */
public interface RequestResolver {

    /**
     * supported framework
     */
    @NotNull
    String getFrameworkName();

    /**
     * find items in project
     */
    List<RestItem> findRestItemInProject(@NotNull Project project);
}