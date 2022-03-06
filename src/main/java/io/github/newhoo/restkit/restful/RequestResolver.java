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
     * scan type
     * SCANNER - spring mvc/structs
     * STORAGE - Local Store/Redis Store
     */
    default ScanType getScanType() {
        return ScanType.SCANNER;
    }

    /**
     * check relative config
     */
    default boolean checkConfig() {
        return true;
    }

    /**
     * find items in project
     */
    List<RestItem> findRestItemInProject(@NotNull Project project);

    /**
     * add api
     *
     * @param itemList not null
     */
    default void add(List<RestItem> itemList) {
    }

    /**
     * update api
     *
     * @param itemList not null
     */
    default void update(List<RestItem> itemList) {
    }

    /**
     * delete api
     *
     * @param itemList not null
     */
    default void delete(List<RestItem> itemList) {
    }

    enum ScanType {
        SCANNER, STORAGE
    }
}