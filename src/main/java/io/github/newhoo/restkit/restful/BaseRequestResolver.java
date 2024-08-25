package io.github.newhoo.restkit.restful;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.ConfigHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * provide module scope search way
 *
 * @author huzunrong
 */
@NotProguard
public abstract class BaseRequestResolver implements RequestResolver {

    @NotNull
    @Override
    public List<RestItem> findRestItemInProject(@NotNull Project project) {
        boolean scanServiceWithLib = ConfigHelper.getCommonSetting(project).isScanServiceWithLib();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        return Arrays.stream(modules)
                     .map(module -> findRestItemListInModule(module, getModuleScope(module, scanServiceWithLib)))
                     .flatMap(Collection::stream)
                     .peek(e -> e.setProject(project.getName()))
                     .collect(Collectors.toList());
    }

    /**
     * find api items in module
     */
    public abstract List<RestItem> findRestItemListInModule(Module module, GlobalSearchScope globalSearchScope);

    private GlobalSearchScope getModuleScope(@NotNull Module module, boolean hasLibrary) {
        if (hasLibrary) {
            return module.getModuleWithLibrariesScope();
        } else {
            return module.getModuleScope();
        }
    }
}
